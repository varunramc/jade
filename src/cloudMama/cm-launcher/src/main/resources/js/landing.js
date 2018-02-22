// Landing module

var landingApp = angular.module('landingApp', ['ngRoute', 'landingControllers']);

landingApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/manage', {
                templateUrl: 'partials/manage.html',
                controller: 'deploymentsCtrl'
            }).
            when('/settings', {
                templateUrl: 'partials/settings.html',
                controller: 'settingsCtrl'
            }).
            otherwise({
                redirectTo: '/settings'
            });
    }]);

landingApp
    .controller(
        'navBarCtrl',
        ['$scope', '$location',
            function ($scope, $location) {
                $scope.isActive = function (location) {
                    return location == $location.path();
                };
            }]);