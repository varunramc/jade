// Shared module

var cmInterfaceBaseUrl = 'http://' + document.domain + '\\:' + __clientCfg.launcherApiPort + '/api/';
angular.module('cm.interface', ['ngResource', 'ui.bootstrap']);

angular.module('cm.interface').config(['$httpProvider',
         function ($httpProvider) {
            $httpProvider.defaults.useXDomain = true;
            delete $httpProvider.defaults.headers.common['X-Requested-With'];
        }
]);

angular.module('cm.interface').factory('jobsApi', ['$resource',
    function ($resource) {
        return $resource(cmInterfaceBaseUrl + 'jobs/job/:id', { id: '@id' }, {});
    }
]);

angular.module('cm.interface').factory('jobLogsApi', ['$resource',
    function ($resource) {
        return $resource(cmInterfaceBaseUrl + 'jobs/job/:id/logs', { id: '@id' }, {});
    }
]);

angular.module('cm.interface').factory('jobMetaDataApi', ['$resource',
    function ($resource) {
        return $resource(cmInterfaceBaseUrl + 'jobs/metadata/', {}, {});
    }
]);