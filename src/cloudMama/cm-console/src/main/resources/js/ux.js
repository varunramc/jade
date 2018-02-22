// UX shared

angular.module('cm.ux', ['cm.interface', 'ngSanitize', 'mgo-angular-wizard']);

angular.module('cm.ux').factory('popups', ['$modal',
    function ($modal) {
        var controllerFactory = function (title, message, headerClass, iconClass) {
            return function ($scope, $modalInstance) {
                $scope.title = title;
                $scope.message = message;

                $scope.ok = function () {
                    $modalInstance.close();
                };

                $scope.headerClass = 'modal-header ' + headerClass;
                $scope.headerIconClass = 'glyphicon ' + iconClass;
            };
        };

        return {
            error: function(message) {
                    $modal.open({
                        templateUrl: 'partials/popup.html',
                        size: 'lg',
                        controller: controllerFactory('Error', message, 'popup-header-error', 'glyphicon-warning-sign')
                    });
                }
        };
    }
]);