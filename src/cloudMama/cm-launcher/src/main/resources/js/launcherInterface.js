// Launcher interface

var launcherInterface = angular.module('launcher.interface', ['cm.interface']);

launcherInterface.factory('deploymentsApi', ['$resource',
    function ($resource) {

        return $resource(cmInterfaceBaseUrl + 'deployments/:id', {}, {
            list: { method: 'GET', params: {}, isArray: true }
        });
    }
]);

launcherInterface.factory('settingsApi', ['$resource',
    function ($resource) {

        return $resource(cmInterfaceBaseUrl + 'settings', {}, {
            get: { method: 'GET', params: {}, isArray: false }
        });
    }
]);