/** 定义首页控制器层 */
app.controller("indexController", function($scope, baseService){

    $scope.findContentByCategoryId = function (categoryId) {
        baseService.sendGet("/content/findContentByCategoryId?categoryId=" + categoryId).then(function (value) {
            $scope.contentList = value.data;
        })
    };

    $scope.search = function () {
        var keywords = $scope.keywords?$scope.keywords:'';
        location.href="http://search.pinyougou.com?keywords=" + keywords;
    };

    $scope.showLoginName = function () {
        baseService.sendGet("/user/showLoginName").then(function (response) {
            $scope.loginName = response.data.loginName;
            $scope.redirectUrl = window.encodeURIComponent(location.href);
        })
    }

});