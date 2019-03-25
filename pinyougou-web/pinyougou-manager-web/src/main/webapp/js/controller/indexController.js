app.controller('indexController', function ($scope, baseService) {

    $scope.showLoginName = function () {
        baseService.sendGet("/showLoginName").then(function (value) {
            $scope.loginName = value.data.loginName;
        })
    }
});