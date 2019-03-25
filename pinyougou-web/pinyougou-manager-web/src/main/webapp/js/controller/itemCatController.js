app.controller("itemCatController", function ($scope, $controller, baseService) {

    $controller("baseController", {$scope: $scope});

    $scope.grade = 1;

    $scope.search = function (parendId) {
        baseService.sendGet("/itemCat/findByParentId?parentId=" + parendId).then(function (reposne) {
            $scope.itemCats = reposne.data;
        });
    }

    $scope.selectList = function (item, grade) {
        $scope.grade = grade;

        if (grade == 1) {
            $scope.itemcat_1 = null;
            $scope.itemcat_2 = null;
        }

        if (grade == 2) {
            $scope.itemcat_1 = item;
            $scope.itemcat_2 = null;
        }

        if (grade == 3) {
            $scope.itemcat_2 = item;
        }

        $scope.search(item.id);
    }



});