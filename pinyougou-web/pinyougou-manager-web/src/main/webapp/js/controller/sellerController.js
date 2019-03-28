app.controller("sellerController", function ($scope, $controller, baseService) {

    $controller("baseController", {$scope: $scope});

    //商家管理(zhang)
    $scope.searchEntity = ['待审核','已审核','审核未通过','关闭'];

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/seller/findAsPage",pageNum, rows, $scope.searchEntity).then(function (value) {
            $scope.sellers = value.data.rows;
            $scope.paginationConf.totalItems = value.data.total;
        });
    };

    $scope.show = function (seller) {
        $scope.seller = JSON.parse(JSON.stringify(seller));
    };

    $scope.updateStatus = function (sellerId, status) {
        baseService.sendGet("/seller/updateStatus?sellerId=" + sellerId + "&status=" + status).then(function (value) {
            if (value.data) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };



});