// Job launcher directive

angular.module('cm.ux')
    .directive('jobLauncher', ['jobMetaDataApi', 'jobsApi', function(jobMetaDataApi, jobsApi) {
        return {
            restrict: 'E',
            scope: {
                package: '@'
            },
            controller: ['$scope', '$modal', 'popups', 'WizardHandler', function ($scope, $modal, popups, WizardHandler) {
                $scope.jobMetadata = jobMetaDataApi.query({ packageName: $scope.package });

                $scope.selectJob = function(jobIndex) {
                    var selectedJob = $scope.jobMetadata[jobIndex];
                    $scope.selectedJob = selectedJob;
                    $scope.selectedJobHasParameters = false;

                    $scope.jobInput = {
                        classCanonicalName: selectedJob.classCanonicalName,
                        parameters: {}
                    };

                    for (var key in selectedJob.parameterMetaDataMap) {
                        $scope.jobInput.parameters[key] = selectedJob.parameterMetaDataMap[key].defaultValue;
                        $scope.selectedJobHasParameters = true;
                    }

                    WizardHandler.wizard().next();
                };

                $scope.launchJob = function() {
                    var newJob = new jobsApi($scope.jobInput);
                    newJob.$save(
                        function (result, responseHeaders) {
                            $scope.newJob = result;
                            WizardHandler.wizard().next();
                        },
                        function (httpResponse) {
                            popups.error(
                                'Job creation failed.'
                                 + '<br/><br/>'
                                 + '<ul><li><b>Server HTTP response:</b> ' + httpResponse.status + '</li>'
                                 + '<li><b>Message:</b> ' + httpResponse.data + '</li></ul>');
                        });
                };
            }],
            templateUrl: 'partials/jobLauncher.html'
        };
    }]);