#! /usr/bin/env python
#
# Deployment script to set up containers locally

import docker
import re
import os
import time
import sys
import socket
import stat
from plumbum import SshMachine

# --- The role instances to deploy locally --- #
roleInstances = \
[
    {
        "Name": "frontEnd1",
        "Role": "frontEnd"
    },
    {
        "Name": "frontEnd2",
        "Role": "frontEnd"
    }
]
# -------------------------------------------- #

if os.name != "nt":
    import fcntl
    import struct

    def get_interface_ip(ifname):
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        return socket.inet_ntoa(fcntl.ioctl(s.fileno(), 0x8915, struct.pack('256s',
                                ifname[:15]))[20:24])

def getLocalIp():
    if len(sys.argv) > 1:
        return sys.argv[1]

    ip = socket.gethostbyname(socket.gethostname())
    if ip.startswith("127.") and os.name != "nt":
        interfaces = [
            "eth0",
            "eth1",
            "eth2",
            "wlan0",
            "wlan1",
            "wifi0",
            "ath0",
            "ath1",
            "ppp0",
            ]
        for ifname in interfaces:
            try:
                ip = get_interface_ip(ifname)
                break
            except IOError:
                pass
    return ip

print '--- Starting local Jade deployment on local machine[{}] ---'.format(getLocalIp())

with docker.Client(base_url = 'unix://var/run/docker.sock') as dockerClient:
    def removeAllLocalContainers():
        print 'Removing all existing local containers'

        existingContainers = filter(lambda c: re.search(r'local_', c['Names'][0]), dockerClient.containers(all = True))
        if existingContainers:
            for container in existingContainers:
                if re.search(r'^Up', container['Status']):
                    print '>>> Container {} is running, stopping it...'.format(container['Id'])
                    dockerClient.stop(container['Id'])

                print '>>> Removing existing container: ', container['Id']
                dockerClient.remove_container(container['Id'])

    def bootstrapCloudMama():
        print 'Bootstrapping CloudMama...'
        cmContainer = dockerClient.create_container(
                            image = 'jade_cloudmama',
                            name = 'local_cmInstance',
                            command = '/bin/bash /opt/jade/startup.sh',
                            ports = [22])
        dockerClient.start(cmContainer, publish_all_ports = True)
        print 'CM instance id = ', cmContainer['Id']
        return dockerClient.port(cmContainer, 22)[0]['HostPort']

    def createRemoteManagementShell(sshPort):
        print 'Creating remote management shell...'

        rmsFile = './rmsConnect.sh'

        with open(rmsFile, 'w') as f:
            lines = \
            [
                "#!/bin/bash\n",
                "ssh -i /home/jade/.ssh/id_rsa -o 'StrictHostKeyChecking no' -p {} jade@localhost\n".format(sshPort)
            ]
            f.writelines(lines)

        os.chmod(rmsFile, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)

    def deployInstance(remote, instance):
        print 'Deploying instance local_' + instance['Name']

        python = remote['python']
        python['/opt/jade/remoteScripts/deployInstance.py', str(getLocalIp()), instance['Role'], instance['Name']]()

    removeAllLocalContainers()
    cmSshPort = bootstrapCloudMama()
    createRemoteManagementShell(cmSshPort)

    print 'Connecting to CM instance at localhost:{} to deploy other roles...'.format(cmSshPort)
    attempt = 0
    while True:
        try:
            if attempt >= 5:
                'Failed to connect to CM instance!'
                sys.exit(1)

            time.sleep(10)
            attempt += 1

            with SshMachine('localhost', port = cmSshPort, user = 'jade', keyfile = '/home/jade/.ssh/id_rsa', ssh_opts = ['-o StrictHostKeyChecking=no']) as remote:
                for instance in roleInstances:
                    deployInstance(remote, instance)
            break;
        except Exception as e:
            print 'Connection attempt failed:'
            print e
            print 'Sleeping before a retry...'

    print 'CM public SSH endpoint is localhost:', cmSshPort

print '--- Deployment is complete. Use the remote management shell to control the environment ---'
