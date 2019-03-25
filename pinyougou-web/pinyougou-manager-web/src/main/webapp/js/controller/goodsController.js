app.controller('goodsController', function ($scope, $controller, baseService) {

    //继承baseController
    $controller("baseController", {$scope: $scope});

    $scope.goodsStatus = ['未审核', '已审核', '审核未通过', '关闭'];

    $scope.searchEntity = {};

    $scope.selectIds = [];

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/goods/findAsPage", pageNum, rows, $scope.searchEntity).then(function (value) {
            $scope.goodsList = value.data.rows;
            $scope.paginationConf.totalItems = value.data.total;
        })
    };

    $scope.updateSelectIds = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            $scope.selectIds.splice($scope.selectIds.indexOf(id), 1);
        }
    };

    $scope.doSelectIds = function (url) {
        url = "/goods/" + url;


        baseService.sendGet(url,"selectIds=" + $scope.selectIds).then(function (value) {
            if (value) {
                $scope.selectIds = [];
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };



});