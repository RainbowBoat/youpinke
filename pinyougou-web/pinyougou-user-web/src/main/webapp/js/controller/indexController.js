app.controller('indexController', function($scope, baseService){

    $scope.showLoginName = function () {
        $scope.redirectUrl = window.encodeURIComponent(location.href);
        baseService.sendGet("/user/showLoginName").then(function (response) {
            $scope.loginName = response.data.loginName;
        })
    }
});