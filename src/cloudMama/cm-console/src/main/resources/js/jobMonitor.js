// Job launcher directive

angular.module('cm.ux')
    .directive('jobMonitor', ['jobsApi', 'jobLogsApi', '$interval', function(jobsApi, jobLogsApi, $interval) {
        return {
            restrict: 'E',
            scope: {
                jobId: '='
            },
            controller: ['$scope', function ($scope) {
                $scope.flattenJobTree = function (job) {
                    var jobState = {
                        id: job.id,
                        type: job.type,
                        status: job.status,
                        progressPercentage: job.progressPercentage,
                        error: job.error
                    };
                    $scope.flattenedJobTree.push(jobState);

                    job.children.map(function (child) {
                        $scope.flattenJobTree(child);
                    });
                };

                $scope.refreshJobStatus = function(firstUpdate) {
                    var job = jobsApi.get({ id: $scope.jobId }, function() {
                        $scope.flattenedJobTree = [];
                        $scope.job = job;
                        $scope.flattenJobTree(job);
                    });

                    if (firstUpdate) {
                        $scope.jobLogs = {};
                    }

                    for (var i in $scope.flattenedJobTree) {
                        var knownJob = $scope.flattenedJobTree[i];

                        var jobLogs = jobLogsApi.query({ id: knownJob.id }, function() {
                            for (var j = 0; j < jobLogs.length; j++) {
                                jobLogs[j].time = new Date(jobLogs[j].timestamp).toLocaleString();
                                $scope.jobLogs[jobLogs[j].id] = jobLogs[j];
                            }
                        });
                    }
                };

                $scope.getLogClass = function (log) {
                    if (log.level == 'ERROR') {
                        return 'danger';
                    }
                    else if (log.level == 'WARNING') {
                        return 'warning';
                    }
                    else if (log.level == 'INFO') {
                        return 'success';
                    }
                    else {
                        return 'active';
                    }
                };

                $scope.getExecutionStatusClass = function (status) {
                    if (status == 'SUCCEEDED') {
                        return 'success';
                    }
                    else if (status == 'FAILED') {
                        return 'danger';
                    }
                    else if (status == 'TIMED_OUT') {
                        return 'warning';
                    }
                };

                //--- ng-table ---//
                /*$scope.logsTableParams = new ngTableParams({
                    page: 1,
                    count: 20,

                });*/

                $scope.refreshJobStatus(true);
            }],
            link: function (scope, element) {
                var timeoutId;

                var stopJobRefreshTimer = function() {
                    $interval.cancel(timeoutId);
                };

                timeoutId = $interval(function() {
                    if (scope.job.status == 'SUCCEEDED' || scope.job.status == 'FAILED' || scope.job.status == 'TIMED_OUT') {
                        stopJobRefreshTimer();
                    }
                    else {
                        scope.refreshJobStatus(false);
                    }
                }, 1000);

                element.on('$destroy', function() {
                    stopJobRefreshTimer();
                });
            },
            templateUrl: 'partials/jobMonitor.html'
        };
    }]);