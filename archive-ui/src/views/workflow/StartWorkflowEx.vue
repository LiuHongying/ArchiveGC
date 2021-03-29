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
        <el-col :span="24">
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
          title: "通知单作废流程",
          icon: "ecm-icon-cancelnotice",
          name: "通知单作废流程",
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
  },
  mounted() {

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