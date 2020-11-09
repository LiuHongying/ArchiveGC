<template>
  <el-container>
    <el-container>
      <el-main>
        <transition name="fade" mode="out-in">
          <router-view :key="$route.fullPath"></router-view>
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>
<script>
export default {
  name: "SearchCenter",
  data() {
    return {
      opens: ["1000"],
      username: "",
      cardList: [],
      clientPermission: 0,
      menuHeight: window.innerHeight - 120,
      isCollapse: false,
      asideWidth: "160px"
    };
  },
  created() {
    let _self = this;
    var user = sessionStorage.getItem("access-user");
    if (user) {
      _self.clientPermission = sessionStorage.getItem(
        "access-clientPermission"
      );
    }
    // _self.$router.push({ path: "/search/fulltextsearch" });
    _self.loadCards();
  },
  methods: {
    handleClose() {
      this.isCollapse = !this.isCollapse;
      this.asideWidth = this.isCollapse ? "50px" : "160px";
      if(!this.isCollapse){
        this.$refs.searchMenu.open("10");
      }
    },
    clickRouter(val) {
      let _self = this;
      _self.$router.push({
        path: "/search/cardSearch",
        query: { id: val }
      });
    },
    loadCards() {
      let _self = this;
      _self.loading = true;

      //alert(_self.parentid);
      axios
        .post("/search/getCardSearchAll", _self.getLang())
        .then(function(response) {
          _self.cardList = response.data.data;
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    }
  }
};
</script>

<style scoped>
body > .el-container {
  padding: 0px;
  margin-top: 0px;
  margin-left: 0px;
  margin-right: 0px;
  margin-bottom: 0px;
}

#app .sidebar-container .nest-menu .el-submenu>.el-submenu__title, #app .sidebar-container .el-submenu .el-menu-item {
   
    background-color: #7171C6 !important;
}

.el-menu, .el-menu-item{
  background-color: #7171C6 !important;
}
.el-submenu {
    list-style: none;
    margin: 0;
    padding-left: 0;
    background-color: #7171C6 !important;
}
.el-menu-item:focus, .el-menu-item:hover {
  background-color: #5a5a9c !important;
}
.el-main {
    display: block;
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    -ms-flex-preferred-size: auto;
    flex-basis: auto;
    overflow: auto;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    padding: 2px;
}
</style>