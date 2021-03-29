<template>
  <div class="menu-wrapper">
    <template>
      <el-menu-item index="/home"  v-if="!outUser">
        <i class="el-icon-s-home menu-white"></i>
        <span slot="title">
          <router-link to="/home">首页</router-link>
        </span>
      </el-menu-item>
      <el-submenu index="workflowCenter" >
	      <template slot="title">
	        <i class="el-icon-date menu-white"></i>
	            <span>任务中心<el-badge :value="totalCount" class="item"></el-badge></span>
	          </template>
	          <el-menu-item index="/workflow/todoTaskNew">
	            <i class="el-icon-edit-outline menu-white"></i>
	            <span slot="title">
	              <router-link to="/workflow/todoTaskNew">待办任务<el-badge :value="totalCount" class="item"></el-badge></router-link>
	            </span>
	          </el-menu-item>
			    <el-menu-item index="/workflow/doneTaskNew">
	            <i class="el-icon-document-checked menu-white"></i>
	            <span slot="title">
	              <router-link to="/workflow/doneTaskNew">已办任务</router-link>
	            </span>
	          </el-menu-item>
	          <el-menu-item index="/workflow/MyWorkflowNew" v-if="!outUser">
	            <i class="el-icon-document menu-white"></i>
	            <span slot="title">
	              <router-link to="/workflow/MyWorkflowNew">我的流程</router-link>
	            </span>
	          </el-menu-item>
            <el-menu-item index="/record/myborrow"  v-if="!outUser">
              <i class="el-icon-s-order menu-white"></i>
              <span slot="title">
                <router-link to="/record/myborrow">我的借阅</router-link>
              </span>
            </el-menu-item>
	        </el-submenu>
      </template>
    <template v-for="item in dataList.menuItems">
      <template v-if="item.submenus && item.url==null">
        <el-submenu :index="item.id+''" :key="item.id">
          <template slot="title">
            <i v-if="item.icon!=null" :class="item.icon + ' menu-white'"></i>
            <span slot="title">{{item.label}}</span>
          </template>
          <template v-for="sitem in item.submenus">
            <el-menu-item v-if="urlIsExt(sitem.url)" @click="clickRouter(sitem.url)" :index="sitem.url" :key="sitem.id" :class="{'submenu-title-noDropdown':!isNest}">
                  <!-- <svg-icon v-if="sitem.icon!=null" :icon-class="sitem.icon"></svg-icon> -->
                  <i v-if="sitem.icon!=null" :class="sitem.icon  + ' menu-white'"></i>
                  {{sitem.label}}
              </el-menu-item>
            <router-link v-else :to="sitem.url" :key="sitem.id">
              <el-menu-item :index="sitem.url" :key="sitem.id" :class="{'submenu-title-noDropdown':!isNest}">
                  <!--<svg-icon v-if="sitem.icon!=null" :icon-class="sitem.icon"></svg-icon>-->
                   <i v-if="sitem.icon!=null" :class="sitem.icon  + ' menu-white'"></i> 
                  {{sitem.label}}
              </el-menu-item>
            </router-link>
          </template>
        </el-submenu>
      </template>
      <template v-else-if="urlIsExt(item.url)">
        <el-menu-item :index="item.id+''" :key="item.id+'_e'" @click="clickRouter(item.url)">
          <!--<svg-icon icon-class="work-flow"></svg-icon>-->
		    <i v-if="item.icon!=null" :class="item.icon + ' menu-white'"></i> 
          {{item.label}}
        </el-menu-item>
      </template>
      <template v-else>
        <router-link :to="item.url" :key="item.id">
          <el-menu-item :index="item.url" :key="item.id+'_e'" :class="{'submenu-title-noDropdown':!isNest}">
             <i v-if="item.icon!=null" :class="item.icon + ' menu-white'"></i> 
            <!--<svg-icon v-if="item.icon!=null" :icon-class="item.icon"></svg-icon>-->
            {{item.label}}
          </el-menu-item>
        </router-link>
      </template>
    </template>
    <template>
      <el-submenu index="userCenter">
	          <template slot="title">
	            <i class="el-icon-user menu-white"></i>
	            <span>{{$t('route.userCenter')}}</span>
	          </template>
	          <el-menu-item index="/user/userinfo">
	            <i class="iconfont zisecm-my_light menu-white"></i>
	            <span slot="title">
	              <router-link to="/user/userinfo">{{$t('route.userInfo')}}</router-link>
	            </span>
	          </el-menu-item>
			  <el-menu-item index="/user/userroleinfo">
	            <i class="iconfont zisecm-group menu-white"></i>
	            <span slot="title">
	              <router-link to="/user/userroleinfo">{{$t('route.userRoleInfo')}}</router-link>
	            </span>
	          </el-menu-item>
	          <el-menu-item index="/user/changepassword">
	            <i class="el-icon-lock menu-white"></i>
	            <span slot="title">
	              <router-link to="/user/changepassword">{{$t('route.changePassword')}}</router-link>
	            </span>
	          </el-menu-item>
            <el-menu-item index="/ShopingCart"  v-if="!outUser">
              <i class="iconfont zisecm-group menu-white"></i>
              <span slot="title">
                <router-link to="/ShopingCart">我的收藏</router-link>
              </span>
            </el-menu-item>
            <el-menu-item index="/Subscription"  v-if="!outUser">
              <i class="iconfont zisecm-group menu-white"></i>
              <span slot="title">
                <router-link to="/Subscription">我的订阅</router-link>
              </span>
            </el-menu-item>
	        </el-submenu>
      </template>
    <AdminMenu v-if="clientPermission>4" :isNest="isNest"></AdminMenu>
  </div>
</template>

<script>
import { generateTitle } from "@/utils/i18n";
import AdminMenu from "./AdminMenu";
export default {
  name: "SidebarItem",
  components: {
    AdminMenu: AdminMenu
  },
  data() {
    return {
      dataList: [],
      clientPermission: 0,
      outUser:false,
      totalCount: 0
    };
  },
  props: {
    isNest: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    hasOneShowingChildren(children) {
      const showingChildren = children.filter(item => {
        return !item.hidden;
      });
      if (showingChildren.length === 1) {
        return true;
      }
      return false;
    },
     getToDoList() {
      let _self = this;
      var m = new Map();
      m.set("condition", "");
      m.set("pageSize", 1);
      m.set("pageIndex", 0);
      m.set("userId", sessionStorage.getItem("access-userName"));
      axios
        .post("/workflow/todoTask", JSON.stringify(m))
        .then(function (response) {
          _self.totalCount = response.data.totalCount;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
     urlIsExt(url){
      if(url!=null&&url.indexOf('http')==0){
        return true;
      }else{
        return false;
      }
      
    },
    clickRouter(pathVal){
    let _self = this;
    if(pathVal.substr(0,4)=='http'){
      window.open(pathVal+"?token="+sessionStorage.getItem('access-token'),'_blank')
    }else{
      _self.$router.push({ 
        path: pathVal,
      });
    }
    

  },
    loadMenu() {
      let _self = this;
      var m = new Map();
      let user = this.currentUser();
      user.roles.forEach(function(item){
        if(item=='文印人员'){
          _self.outUser = true
        }
      });
      if(_self.outUser){
        m.set("name", "OutUserMenu");
      }else{
        m.set("name", "TopMenu");
      }
       //m.set("name","cnpeexchange");
      m.set("lang", _self.getLang());
      axios
        .post("/memu/getMyMenu", JSON.stringify(m))
        .then(function(response) {
          //console.log(response);
          if (response.data.code == 1) {
            _self.dataList = response.data.data;
          }
          //console.log(_self.dataList);
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    }
  },
  created() {
    this.loadMenu();
  },
  mounted(){
    if (this.currentUser()) {
        this.clientPermission = Number(
          this.currentUser().clientPermission
        );
      }
    this.getToDoList();
  },
  watch:{
    '$store.state.app.language':function(nv,ov){
      this.loadMenu()
    }
  },
};
</script>
<style scoped>
</style>