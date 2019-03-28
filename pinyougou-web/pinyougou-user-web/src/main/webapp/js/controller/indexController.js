app.controller('indexController', function($scope,$controller,$location,$timeout,baseService){
    $scope.showLoginName = function () {
        $scope.redirectUrl = window.encodeURIComponent(location.href);
        baseService.sendGet("/user/showLoginName").then(function (response) {
            $scope.loginName = response.data.loginName;
        })
    };
    //图片上传
    $scope.uploadPicture= function () {
        baseService.uploadFile().then(function (value) {
            if (value.status == 200) {
                $scope.user.headPic = value.data.url;
            }
        });
    };
    /** 定义用户对象 */
    $scope.user = {nickName:"",sex:"",birthday:"",headPic:"",address:{}};
    /** 添加或修改 */
    $scope.saveOrUpdate = function(){
        var url = "saveUser";
        /** 发送post请求 */
        baseService.sendPost("/user/" + url, $scope.user)
            .then(function(response){
                if (response.data){
                    alert("加载成功")
                }else{
                    alert("操作失败！");
                }
            });
    };
    /**
     * 地区分类选择
     */
    $scope.findProvincesByParentId = function (name) {
        baseService.sendGet("/user/findAll").then(function (value) {
                $scope[name] = value.data;

        });
    };
    $scope.$watch('user.address.provinceId', function (newVal, oldVal) {
        if (newVal) {
            var num=newVal-1;

            $scope.findProvincesByTwo(JSON.stringify($scope.ProvincesList1[num].provinceId));
        } else {
            $scope.Provinces1 = [];
        }
    });
    $scope.findProvincesByTwo=function(provinceId){
        baseService.sendGet("/user/findProvincesByTwo?provinceId="+JSON.parse(provinceId)).then(function(response){
                $scope.ProvincesList2=response.data
        })
    }
    $scope.$watch('user.address.cityId', function (newVal, oldVal) {
        if (newVal) {
            var num=newVal-1;
            $scope.findProvincesByThree(JSON.stringify($scope.ProvincesList2[num].cityId));
        } else {
            $scope.Provinces2 = [];
        }
    });
    $scope.findProvincesByThree=function(cityId){
        baseService.sendGet("/user/findProvincesByThree?cityId="+JSON.parse(cityId)).then(function(response){
            $scope.ProvincesList3=response.data
        })
    }



    //用户密码设置
    $scope.submit =function(){
        if($scope.password!=$scope.user.password){
            alert("密码不一致");
            $scope.password="";
            $scope.user.password="";
            return;
        }
        baseService.sendPost("/user/submit",$scope.user).then(function(response){
            if(response.data){
                alert("提交成功")
                $scope.user={};
                $scope.password="";
            }else{
                alert("提交失败")
            }
        })
    }
    /** 定义发送短信验证码方法 */
    $scope.sendCode = function () {
        if ($scope.phone){
            baseService.sendGet("/user/sendCode?phone="+ $scope.phone).then(function(response){
                alert(response.data)
                    alert(response.data ? "发送成功！" : "发送失败！");

                });
        }else {
            alert("请输入手机号码！");
        }
    };
    //判断验证码是否正确，绑定手机的下一步
    $scope.Click=function(){
        baseService.sendGet("/user/clickJudge?code="+$scope.code+"&phone="+$scope.phone+"&codes="+$scope.codes).then(function(response){
            if(response.data==false){
                alert("请输入正确的验证码")
            }else if($scope.code==null){
                alert("验证码不能为空")

            }else if(response.data==true){
                location.href="http://user.pinyougou.com/home-setting-address-phone.html";
            }
        })
    };
    $scope.ClickTwo=function(){
        baseService.sendGet("/user/clickJudge?code="+$scope.code+"&phone="+$scope.phone+"&codes="+$scope.codes).then(function(response){
            if(response.data==false){
                alert("请输入正确的验证码")
            }else if($scope.code==null){
                alert("验证码不能为空")

            }else if(response.data==true){
                location.href="http://user.pinyougou.com/home-setting-address-complete.html";
            }
        })
    };
  //显示手机号
    $scope.findNumber =function(){
        baseService.sendGet("/user/findNumber").then(function(response){
            $scope.phone = response.data;

        })
    };
    // 发送短信验证码
    $scope.sendMsg = function () {
        if ($scope.phone && /^1[3|4|5|7|8|9]\d{9}$/.test($scope.phone)) {
            baseService.sendGet("/user/sendCode?phone=" + $scope.phone).then(function (response) {
                // 获取响应数据
                if (response.data){

                    // 倒计时 (扩展)
                    $scope.flag = true;
                    // 调用倒计时方法
                    $scope.downCount(90);
                }else{
                    alert("获取短信验证码失败！");
                }

            })
        } else {
            alert("手机号输入有误");
        }
    };
    $scope.tipMsg = "获取短信验证码";
    $scope.flag = false;
    //获取验证码几时器
    $scope.downCount = function (second) {
        if (second > 0) {
            second--;
            $scope.tipMsg = second + "秒后重新获取验证码";

            //开启定时器
            $timeout(function () {
                $scope.downCount(second);
            },1000)
        } else {
            $scope.tipMsg = "获取短信验证码";
            $scope.flag = false;
        }
    }
});