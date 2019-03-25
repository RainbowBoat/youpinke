app.controller("baseController", function ($scope) {
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 0,
        itemsPerPage: 10,
        perPageOptions: [10, 15, 20],
        onChange: function () {
            $scope.reload();
        }
    };

    $scope.reload = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    $scope.selections = [];

    $scope.updateSelection = function ($event, id, i) {
        if ($event.target.checked) {
            $scope.selections.push(id);
        } else {
            var idx = $scope.selections.indexOf(id);
            $scope.selections.splice(idx, 1);
        }
        //重新赋值, 再次绑定checkbox
        $scope.checkedArr[i] = $event.target.checked;
        //让全选是否选中, 再次绑定checkbox
        $scope.ckAll = $scope.selections.length == $scope.dataList.length;
    };

    $scope.checkedArr = [];

    //为全选按钮绑定点击事件
    $scope.checkAll = function ($event) {
        //清空用户的选择
        $scope.selections = [];

        //循环当前页的数组, 给每个checkbox都绑定选中
        for (var i = 0; i < $scope.dataList.length; i++) {

            $scope.checkedArr[i] = $event.target.checked;

            //选中后放进数组
            if ($event.target.checked) {

                $scope.selections.push($scope.dataList[i].id);
            }
        }

        $scope.ckAll = $scope.selections.length == $scope.dataList.length;
    }
});