<template>
  <el-container>
    <el-main>
      <div>
        <el-dialog
          :title="dialogtitle"
          append-to-body
          :visible.sync="dialogVisable"
          @close="closePage"
          width="90%"
          :close-on-click-modal="false"
          v-dialogDrag
        >
          <component
            ref="comp"
            :is="dialogComponent"
            :workflowObj="workflow"
            :typeName="workflow.FORMNAME"
            @closedialog="closeDialog"
          ></component>
        </el-dialog>
      </div>
      <el-row>
        <el-col :span="16">
          <el-card :body-style="{ height: '120px' }" v-if="!outUser">
            <div slot="header" class="clearfix" style="padding-bottom: 5px">
              <span style="float: left" class="ecmtitle">{{
                $t("route.fullTextSearch")
              }}</span>
            </div>
            <div>
              <el-row style="padding-top: 5px; padding-bottom: 5px">
                <!-- <el-autocomplete
                  class="inline-input"
                  v-loading="loading"
                  prefix-icon="el-icon-search"
                  :fetch-suggestions="querySearch"
                  :placeholder="$t('application.placeholderSearch')"
                  :trigger-on-focus="false"
                  @keyup.enter.native="jumpToFullSearch"
                  style="width:80%;"
                  v-model="inputkey"
                ></el-autocomplete> -->
                <el-input
                  prefix-icon="el-icon-search"
                  @keyup.enter.native="jumpToFullSearch"
                  style="width: 70%"
                  v-model="inputkey"
                  :placeholder="$t('application.placeholderSearch')"
                ></el-input>
                <el-button style="margin-left:5px;width:60px" type="primary" plain @click="jumpToFullSearch">{{$t('application.search')}}</el-button>
                <el-checkbox
                  style="margin-left:5px"
                  :label="$t('application.propertyOnly')"
                  v-model="propertyOnly"
                ></el-checkbox>
              </el-row>
              <el-row>
                <el-row
                  style="
                    padding-top: 5px;
                    padding-bottom: 5px;
                    float: left;
                    text-align: left;
                  "
                >
                  <span class="ecmcontent">{{
                    $t("application.docTypeName")
                  }}</span>
                  <el-checkbox
                    :indeterminate="isIndeterminate"
                    v-model="checkAll"
                    @change="handleCheckAllChange"
                    >{{ $t("application.selectAll") }}</el-checkbox
                  >
                </el-row>
                <el-row
                  style="
                    padding-top: 5px;
                    padding-bottom: 5px;
                    float: left;
                    text-align: left;
                  "
                >
                <el-checkbox-group v-model="checkedCards">
                    <el-col :xs="8" :sm="6" :md="6" :lg="4" :xl="3" v-for="card in cards" :key="card.id">
                    <el-checkbox
                      :label="card.name"
                      @change="handleCheckedTypeChange"
                    >{{card.name}}</el-checkbox>
                    </el-col>
                </el-checkbox-group>
                </el-row>
              </el-row>
            </div>
          </el-card>
          <el-card :body-style="{ height: '160px' }">
            <div slot="header" class="clearfix" style="padding-bottom: 5px">
              <span style="float: left" class="ecmtitle"
                >待办任务<el-badge :value="totalCount" class="item"></el-badge>
              </span>
              <el-link
                :underline="false"
                style="float: right; padding: 3px 0"
                @click="$router.push(jumpPath.todolist)"
                type="primary"
                >更多>></el-link
              >
            </div>
            <el-table
              :data="dataList.todoData"
              v-loading="loadingTodoData"
              style="width: 100%"
              :show-header="false"
            >
              <el-table-column label="任务名称">
                <el-link
                  slot-scope="scope"
                  type="primary"
                  @click="openTask(scope.row.id)"
                  >{{ scope.row.name }}</el-link
                >
              </el-table-column>
              <el-table-column
                prop="startUser"
                label="发送人"
                class="ecmcontent"
              ></el-table-column>
              <el-table-column
                label="发送时间"
                align="right"
                class="ecmcontent"
              >
                <template slot-scope="scope">{{
                  dateFormat(scope.row.createTime)
                }}</template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- <marquee behavior="scroll" scrollamount=26>
              <template v-for="item in imagesBox">
                <img :src="item">
              </template>
              <template v-for="item in imagesBox">
                <img :src="item">
              </template>
            </marquee> -->
          <!-- <PaoMaDeng :delay="0.5" :speed="100" :content="imagesBox">
               <span v-for="(item, index) in imagesBox" :key="index">
                 <img :src="item">
               </span>
             </PaoMaDeng> -->
          <el-card :body-style="{ height: '280px' }" v-if="!outUser">
            <div slot="header" class="clearfix" style="padding-bottom: 5px">
              <span style="float: left" class="ecmtitle">业务流程</span>
            </div>
            <el-row>
              <el-col
                :xs="12"
                :sm="8"
                :md="6"
                :lg="6"
                :xl="4"
                v-for="item in icons"
                :key="item.title"
              >
                <div
                  :title="item.title"
                  :class="item.icon"
                  @click="onIconClick(item)"
                ></div>
                <div class="ecm-icon-desc">{{ item.name }}</div>
              </el-col>
            </el-row>
          </el-card>
          <!-- <el-card :body-style="{ height: '220px' }">
            <div slot="header" class="clearfix" style="padding-bottom:5px;">
              <span style="float: left;" class="ecmtitle">最新文档</span>
            </div>
            <el-table
              v-loading="loadingNewDocData"
              :data="dataList.newdocData"
              style="width:100%;"
              :show-header="false"
            >
              <el-table-column prop="CODING" label="编码" width="240" class="ecmcontent"></el-table-column>
              <el-table-column width="60" prop="REVISION" label="版本" class="ecmcontent"></el-table-column>
              <el-table-column label="题名" min-width="20%" :show-overflow-tooltip="true">
                <template slot-scope="scope">
                  <el-link  type="primary" @click="showFile(scope.row)">{{scope.row.NAME}}</el-link>
                </template>
              </el-table-column>
              <el-table-column label="编制日期" align="right" width="120" class="ecmcontent">
                <template slot-scope="scope">{{dateFormat(scope.row.C_DOC_DATE)}}</template>
              </el-table-column>
            </el-table>
          </el-card> -->
        </el-col>
        <el-col :span="8">
          <el-card :body-style="{ height: '100px'}">
            <div slot="header" class="clearfix" style="padding-bottom: 5px">
              <span style="float: left" class="ecmtitle">个人中心</span>
            </div>
            <el-row class="person-row">
            <el-col :span="8" v-if="!outUser">
              <i style="font-size: 28px;" class="el-icon-star-off el-checkbox__label"></i>
              <el-link
                :underline="false"
                @click="$router.push('/ShopingCart')"
                >我的收藏</el-link
              >
            </el-col>
            <el-col :span="8">
              <i style="font-size: 28px;" class="el-icon-user el-checkbox__label"></i>
              <el-link
                :underline="false"
                @click="$router.push(jumpPath.userCenter)"
                >我的信息</el-link
              >
            </el-col>
            <el-col :span="8">
              <i style="font-size: 28px;" class="el-icon-finished el-checkbox__label"></i>
              <el-link
                :underline="false"
                @click="$router.push('/user/userroleinfo')"
                >我的授权</el-link
              >
            </el-col>
            </el-row>
          </el-card>
          <el-card :body-style="{ height: '300px' }">
            <div slot="header" class="clearfix" style="padding-bottom: 5px">
              <span style="float: left" class="ecmtitle">通知公告</span>
              <el-link
                :underline="false"
                @click="routerJump('通知公告')"
                style="float: right; padding: 3px 0"
                type="primary"
                >更多>></el-link
              >
            </div>
            <el-table
              v-loading="loadingNoticeData"
              :data="dataList.notiData"
              style="width: 100%"
              size="small"
              :show-header="false"
            >
              <el-table-column
                :show-overflow-tooltip="true"
                min-width="60%"
                label="标题"
              >
                <el-link
                  slot-scope="scope"
                  type="primary"
                  @click="showFile(scope.row)"
                  >{{ scope.row.NAME }}</el-link
                >
              </el-table-column>
              <el-table-column align="right" width="120">
                <template slot-scope="scope">{{
                  dateFormat(scope.row.CREATION_DATE)
                }}</template>
              </el-table-column>
            </el-table>
          </el-card>
          <!-- <el-card :body-style="{ height: '310px' }">
            <div slot="header" class="clearfix" style="padding-bottom: 5px">
              <span style="float: left" class="ecmtitle">管理文件</span>
              <el-link
                :underline="false"
                @click="routerJump('管理文件')"
                style="float: right; padding: 3px 0"
                type="primary"
                >更多>></el-link
              >
            </div>
            <el-table
              v-loading="loadingReData"
              :data="dataList.regulationData"
              style="width: 100%"
              size="small"
              :show-header="false"
            >
              <el-table-column
                :show-overflow-tooltip="true"
                min-width="60%"
                label="标题"
              >
                <el-link
                  slot-scope="scope"
                  type="primary"
                  @click="showFile(scope.row)"
                  >{{ scope.row.NAME }}</el-link
                >
              </el-table-column>
              <el-table-column align="right" width="120">
                <template slot-scope="scope">{{
                  dateFormat(scope.row.CREATION_DATE)
                }}</template>
              </el-table-column>
            </el-table>
          </el-card> -->
          <!-- <el-card :body-style="{ height: '310px',width:'100%' }">
            <div slot="header" class="clearfix" style="padding-bottom:5px;">
              <span style="float: left;" class="ecmtitle">馆藏状态</span>
            </div>
            <div id="collectionChart" style="height: 100%;width: 100%;padding-bottom:20px;"></div>
          </el-card> -->
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>
<script>
import BorrowStartUp from "@/views/workflow/BorrowStartUp.vue";
import CopyStartUp from "@/views/workflow/CopyStartUp.vue";
import DesignCancelStartUp from "@/views/workflow/DesignCancelStartUp.vue";
import DesignChangeFileStartup from "@/views/workflow/DesignChangeFileStartup.vue";
import RelyOnFolderSelectStartUp from "@/views/workflow/RelyOnFolderSelectStartUp.vue";
import CancelStartUp from "@/views/workflow/CancelStartUp.vue";
import ViolationStartUp from "@/views/workflow/ViolationStartUp.vue";
import RelyOnFolderSelectStartup4Modify from "@/views/workflow/RelyOnFolderSelectStartup4Modify.vue"
export default {
  data() {
    return {
      jumpPath: {
        search: "",
        todolist: "/workflow/todoTaskNew",
        notification: "/dc/folderviewer",
        userCenter: "/user/userinfo",
      },
      imagesBox: [],
      collectionChartData: {
        xAxisData: [],
        yAxisData: [],
      },
      dataList: {
        todoData: [],
        newdocData: [],
        notiData: [],
        carouselData: [],
        regulationData: [],
      },
      icons: [
        {
          title: "文档借阅",
          icon: "ecm-icon-borrow",
          name: "文档借阅流程",
          type: "dialog",
          openpath: "BorrowStartUp",
        },
        {
          title: "文档复制",
          icon: "ecm-icon-cp",
          name: "文档复制流程",
          type: "dialog",
          openpath: "CopyStartUp",
        },
        {
          title: "文档违规处理",
          icon: "ecm-icon-docviolatehandle",
          name: "文档违规处理流程",
          type: "dialog",
          openpath: "ViolationStartUp",
        },
        {
          title: "设计文件修改",
          icon: "ecm-icon-designdoc",
          name: "设计文件修改流程",
          type: "dialog",
          openpath: "DesignChangeFileStartup",
        },
        {
          title: "科研预归档借阅",
          icon: "ecm-icon-prearchive",
          name: "科研预归档借阅流程",
          type: "dialog",
          openpath: "RelyOnFolderSelectStartUp",
        },
        {
          title: "科研文件修改",
          icon: "ecm-icon-scdoc",
          name: "科研文件修改流程",
          type: "dialog",
          openpath: "RelyOnFolderSelectStartup4Modify",
        },
        {
          title: "作废通知单",
          icon: "ecm-icon-cancelnotice",
          name: "设计文件作废流程",
          type: "dialog",
          openpath: "CancelStartUp",
        },
      ],
      dialogVisable: false,
      dialogComponent: "",
      inputkey: "",
      scroll: true,
      loading: false,
      loadingTodoData: false,
      loadingNewDocData: false,
      loadingNoticeData: false,
      loadingReData: false,
      checkAll: false,
      isIndeterminate: false,
      checkedCards: [],
      propertyOnly: false,
      cards: [],
      cardsLabel: [],
      keywords: [],
      totalCount: 0,
      groupData: [],
      collectionChart: Object,
      dialogVisible: false,
      workflow: {},
      dialogtitle:"",
      outUser:false
    };
  },
  components: {
    BorrowStartUp: BorrowStartUp,
    CopyStartUp: CopyStartUp,
    DesignCancelStartUp: DesignCancelStartUp,
    DesignChangeFileStartup: DesignChangeFileStartup,
    RelyOnFolderSelectStartUp: RelyOnFolderSelectStartUp,
    CancelStartUp: CancelStartUp,
    ViolationStartUp: ViolationStartUp,
    RelyOnFolderSelectStartup4Modify:RelyOnFolderSelectStartup4Modify
  },
  created() {
    let _self = this;
     let user = this.currentUser();
      user.roles.forEach(function(item){
        if(item=='文印人员'){
          _self.outUser = true
        }
      });
    _self.getToDoList();
    if(!_self.outUser){
      _self.loadCards();
    }
    _self.getNewsList();
    //_self.getDocument();
    _self.getCarousel();
    //_self.getRegulation();
  },
  mounted() {
    let _self = this;
    _self.docChart5 = _self.echarts.init(document.getElementById("docChart5"));
  },
  methods: {
    onIconClick(item) {
      let _self = this;
      let _type = item.type;
      _self.dialogtitle = item.title;
      _self.workflow = {};

      var m = new Map();
      m.set("processDefinitionKey", item.name);

      axios
        .post("/dc/getWorkflow", JSON.stringify(m))
        .then(function (response) {
          _self.workflow = response.data.data[0];
          console.log(_self.workflow);
          _self.$nextTick(() => {
            if (_type == null || _type == undefined) {
              _type = "dialog";
            }
            if (_type == "dialog") {
              _self.dialogComponent = item.openpath;
              _self.dialogVisable = true;
            }
          });
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    closeDialog(val) {
      this.dialogVisable = val;
    },
    closePage() {
      this.$refs.comp.clean()
      this.dialogVisable = false
    },
    //获取待办任务列表，最多五条
    getToDoList() {
      let _self = this;
      var m = new Map();
      _self.loadingTodoData = true;
      m.set("condition", "");
      m.set("pageSize", 5);
      m.set("pageIndex", 0);
      m.set("userId", sessionStorage.getItem("access-userName"));
      axios
        .post("/workflow/todoTask", JSON.stringify(m))
        .then(function (response) {
          _self.dataList.todoData = response.data.data;
          _self.totalCount = response.data.totalCount;
          _self.loadingTodoData = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    //获取通知公告栏信息列表
    getNewsList() {
      let _self = this;
      _self.loadingNoticeData = true;
      var m = new Map();
      m.set("gridName", "NewsGrid");
      m.set("folderId", "");
      m.set("condition", " type_name='通知公告' ");
      m.set("pageSize", 5);
      m.set("pageIndex", 0);
      m.set("orderBy", " CREATION_DATE DESC");
      axios
        .post("/dc/getDocuments", JSON.stringify(m))
        .then(function (response) {
          _self.dataList.notiData = response.data.data;
          _self.loadingNoticeData = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    //获取最新的5条新建文档
    getDocument() {
      let _self = this;
      _self.loadingNewDocData = true;
      var m = new Map();
      m.set("pageSize", 6);
      m.set("pageIndex", 0);
      m.set("orderBy", " C_DOC_DATE DESC");
      axios
        .post("/dc/getNewDocuments", JSON.stringify(m))
        .then(function (response) {
          _self.dataList.newdocData = response.data.data;
          _self.loadingNewDocData = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    //获取法律法规数据
    getRegulation() {
      let _self = this;
      var m = new Map();
      _self.loadingReData = true;
      m.set("gridName", "GeneralDocGrid");
      m.set("folderPath", "/表单/管理文件");
      m.set("condition", "");
      m.set("pageSize", 8);
      m.set("pageIndex", 0);
      m.set("orderBy", " CREATION_DATE DESC");
      axios
        .post("/dc/getDocsByFolderPathName", JSON.stringify(m))
        .then(function (response) {
          _self.dataList.regulationData = response.data.data;
          _self.loadingReData = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loadingReData = false;
        });
    },
    //获取轮播图数据
    getCarousel() {
      let _self = this;
      var m = new Map();
      var imgArr = [];
      m.set("gridName", "GeneralDocGrid");
      m.set("folderPath", "/表单/轮播图");
      m.set("pageSize", 5);
      m.set("condition", "");
      m.set("pageIndex", 0);
      m.set("orderBy", " CREATION_DATE DESC");
      axios
        .post("/dc/getDocsByFolderPathName", JSON.stringify(m))
        .then(function (response) {
          _self.dataList.carouselData = response.data.data;
          // var mycars=new Array(response.data.data.size)
          _self.dataList.carouselData.forEach(function (val, index, arr) {
            imgArr[index] =
              _self.axios.defaults.baseURL +
              "/dc/getContent?id=" +
              val.ID +
              "&token=" +
              sessionStorage.getItem("access-token");
          });
          _self.imagesBox = imgArr;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    //加载全文搜索的勾选项
    loadCards() {
      let _self = this;
      var m = new Map();
      axios
        .post("/admin/getArchivesFolder", 0)
        .then(function (response) {
          _self.cards = response.data.data;
          var i = 0;
          for (i = 0; i < _self.cards.length; i++) {
            _self.cardsLabel[i] = _self.cards[i].name;
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    //点击checkbox的事件
    handleCheckedTypeChange() {
      let checkedCount = this.checkedCards.length;
      this.checkAll = checkedCount === this.cardsLabel.length;
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.cards.length;
    },
    handleCheckAllChange(val) {
      this.checkedCards = val ? this.cardsLabel : [];
      this.isIndeterminate = false;
    },
    jumpToFullSearch() {
      var map = new Map();
      let _self = this;
      map.set("inputkey", _self.inputkey);
      map.set("propertyOnly", _self.propertyOnly);
      map.set("checkedCards", _self.checkedCards);
      // console.log(_self.checkedCards)
      _self.$router.push({
        name: "全文搜索",
        params: {
          map: map,
        },
      });
    },
    showFile(indata) {
      let condition = indata.ID;
      let href = this.$router.resolve({
        path: "/viewdoc",
        query: {
          id: condition,
          //token: sessionStorage.getItem('access-token')
        },
      });
      //console.log(href);
      window.open(href.href, "_blank");
    },
    getMyGroup() {
      this.dialogVisible = true;
      let _self = this;
      var userName = sessionStorage.getItem("access-userName");
      axios
        .post("/user/getGroupByUserName", userName)
        .then(function (response) {
          _self.groupData = response.data.data;
        });
    },
    querySearch(queryString, cb) {
      let _self = this;
      _self.loading = true;
      _self;
      axios
        .post("/search/getSuggestion", JSON.stringify(_self.inputkey))
        .then(function (response) {
          _self.keywords = response.data.data;
          if (_self.keywords) {
            var list = [{}];
            var i;
            for (i = 0; i < _self.keywords.length; i++) {
              var item = new Object();
              item.value = _self.keywords[i];
              list[i] = item;
            }
            _self.keywords = list;
          }
          cb(_self.keywords);
          //console.log(JSON.stringify(_self.taskList));
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    openTask(taskId) {
      let _self = this;
      _self.$router.push({
        path: _self.jumpPath.todolist,
        query: {
          openTaskFromMainPage: 1,
          taskId: taskId,
        },
      });
    },
    routerJump(folderName) {
      let _self = this;
      _self.$router.push({
        path: "/dc/folderviewer",
        query: { cfgName: folderName },
      });
    },
  },
};
</script>
<style scoped>
.el-col {
  padding-right: 5px;
  padding-left: 5px;
}
.el-card {
  /* padding-bottom: 30px; */
  margin-bottom: 10px;
}
.el-table {
  width: 100%;
}
.searchInput {
  display: inline;
}
.docType {
  display: inline;
  font-size: 14px;
}
.search {
  padding-right: 20%;
}
.el-card__header {
  background-color: rgb(235, 235, 235);
}
.el-carousel__item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.carousel-image {
  max-width: 100%;
  max-height: 100%;
}
.ecm-icon-desc {
  padding: 0px 0px 30px 0px;
}
.person-row {
  padding: 35px 0px 40px 0px;
}
</style>