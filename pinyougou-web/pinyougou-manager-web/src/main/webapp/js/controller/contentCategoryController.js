app.controller('contentCategoryController', function ($scope, $controller, baseService) {

    //继承baseController
    $controller("baseController", {$scope: $scope});

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/contentCategory/findAsPage", pageNum, rows).then(function (value) {
            $scope.contentCategoryList = value.data.rows;
            $scope.paginationConf.totalItems = value.data.total;
        })
    };

    $scope.remove = function () {
        baseService.deleteById("/contentCategory/remove", $scope.selections).then(function (value) {
            if (value) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };

    $scope.saveOrUpdate = function () {
        var url = "/contentCategory/save";
        if ($scope.contentCategory.id) {
            url = "/contentCategory/update";
        }
        baseService.sendPost(url, $scope.contentCategory).then(function (value) {
            if (value) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };

    $scope.show = function (entity) {
        $scope.contentCategory = JSON.parse(JSON.stringify(entity));
    };



});