// Landing module

var landingControllers = angular.module('landingControllers', ['launcher.interface', 'cm.ux']);

landingControllers
    .controller(
        'deploymentsCtrl',
        ['$scope', '$modal', 'popups', 'deploymentsApi', 'jobsApi',
            function ($scope, $modal, popups, deploymentsApi, jobsApi) {
                $scope.refreshDeployments = function() {
                    $scope.deployments = deploymentsApi.list();
                };

                $scope.openNewDeploymentWindow = function() {
                    var modalInstance = $modal.open({
                        templateUrl: 'partials/newDeploymentWindow.html',
                        size: 'lg',
                        backdrop: 'static',
                        controller: newDeploymentWindowCtrl
                    });

                    modalInstance.result.then(function () {
                        $scope.refreshDeployments();
                    });
                };

                $scope.refreshDeployments();
            }]);

landingControllers
    .controller(
        'settingsCtrl',
        ['$scope', 'settingsApi',
            function ($scope, settingsApi) {
                $scope.settings = settingsApi.get();
            }]);

var newDeploymentWindowCtrl = function ($scope, $modalInstance) {
    $scope.ok = function () {
        $modalInstance.close();
    };

    $scope.cancel = function() {
        $modalInstance.dismiss();
    };
};
