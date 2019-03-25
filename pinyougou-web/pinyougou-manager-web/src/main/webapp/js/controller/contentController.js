app.controller('contentController', function ($scope, $controller, baseService) {

    //继承baseController
    $controller("baseController", {$scope: $scope});

    $scope.search = function (pageNum, rows) {
        baseService.findAsPage("/content/findAsPage", pageNum, rows).then(function (value) {
            $scope.dataList = value.data.rows;
            $scope.paginationConf.totalItems = value.data.total;
        })
    };

    $scope.show = function (entity) {
        $scope.entity = JSON.parse(JSON.stringify(entity));
    };

    $scope.uploadPic = function () {
        baseService.uploadFile().then(function (value) {
            if (value.status == 200) {
                $scope.entity.pic = value.data.url;
            }
        })
    };

    $scope.saveOrUpdate = function () {
        var url = "save";
        if ($scope.entity.id) {
            url = "update";
        }
        baseService.sendPost("/content/" + url, $scope.entity).then(function (value) {
            if (value) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };

    $scope.searchCategory = function () {
        baseService.sendGet("/contentCategory/findAll").then(function (value) {
            $scope.categories = value.data;
        });
    };

    $scope.remove = function () {
        baseService.deleteById("/content/remove", $scope.selections).then(function (value) {
            $scope.selections = [];
            if (value) {
                $scope.reload();
            } else {
                alert("操作失败");
            }
        })
    };

    $scope.status = ['无效', '有效'];

    $scope.selectStatus = function ($event) {
        $scope.entity.status = $event.target.checked ? 1 : 0 ;
    }
});
