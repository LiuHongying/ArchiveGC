<template>
  <div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      v-if="dialogVisible"
      width="95%"
      style="width:100%"
      custom-class="customWidth"
    >
      <el-form style="padding-bottom:30px">
          <el-form-item
          label="流程名称"
          :label-width="formLabelWidth"
          style="float:left"
        >{{currentData.workflowName}}</el-form-item>
        <el-form-item
          label="任务名称"
          :label-width="formLabelWidth"
          style="float:left"
        >{{currentData.name}}</el-form-item>
        <el-form-item
          label="发起人"
          :label-width="formLabelWidth"
          style="float:left"
        >{{currentData.startUser}}</el-form-item>
        <el-form-item
          label="到达时间"
          :label-width="formLabelWidth"
          style="float:left"
        >{{dateFormat(currentData.createTime,'')}}</el-form-item>
      </el-form>
      <!-- <el-divider content-position="left">表单信息</el-divider> -->

      <component style="width:100%"
        ref="propertiesComp"
        :is="taskName"
        v-model="formData"
        :formId="form.formId"
        :allowEdit="allowEdit"
        :docId="form.formId"
        :taskForm="formData"
        :istask="1"
        :formParameter="formParameter"
        :processDefinitionId="currentData.processDefinitionId"
        :activityName="currentData.name"
        :formEditPermision="formEditPermision"
        :formEnableType="this.$options.name"
        :isShowReject="isShowReject"
        @click="click"
      ></component>
      <el-divider content-position="left">流转意见</el-divider>
      <el-table :data="taskList" border v-loading="loading" style="width: 100%">
        <el-table-column label="序号" width="65">
          <template slot-scope="scope">
            <div v-if="scope.row.endTime==null||scope.row.endTime==''">
              <el-tooltip class="item" effect="light" content="未完成" placement="right">
                <el-button type="success" round>{{scope.$index+1}}</el-button>
              </el-tooltip>
            </div>
            <div v-else>
              <el-tooltip class="item" effect="light" content="已完成" placement="right">
                <el-button type="info" round>{{scope.$index+1}}</el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" width="160"></el-table-column>
        <el-table-column prop="assignee" label="用户" width="100"></el-table-column>
        <el-table-column
          prop="createTime"
          label="开始时间"
          sortable
          :formatter="dateFormatter"
          width="180"
        ></el-table-column>
        <el-table-column
          prop="endTime"
          label="完成时间"
          sortable
          :formatter="dateFormatter"
          width="180"
        ></el-table-column>
        <el-table-column prop="result" label="完成结果" width="100"></el-table-column>
        <el-table-column prop="message" label="审批意见" min-width="15%"></el-table-column>
      </el-table>
      <el-divider content-position="left">批注意见</el-divider>
      <el-form :model="form">
        <el-row>
          <div v-if="!isCompleteSelected">
            <el-col>
              <el-form-item label="通过类型" :label-width="formLabelWidth" style="float:left">
                <el-radio-group v-model="form.result">
                  <template v-for="(itm,key) in sequenceFlow">
                    <el-radio-button :label="itm" :key="'seqence'+key">{{itm}}</el-radio-button>
                  </template>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col>
              <el-form-item label="审批意见" :label-width="formLabelWidth">
                <el-input
                  type="textarea"
                  :autosize="{minRows:3}"
                  v-model="form.message"
                  auto-complete="off"
                ></el-input>
              </el-form-item>
            </el-col>
          </div>
        </el-row>
        <div v-show="delegateDialogVisible" style="padding-top:3px;">
          <el-row>
            <el-col :span="6" style="float:right">
              <el-button @click="delegateTask(form)">确认委托</el-button>
            </el-col>
            <el-col :span="8" style="float:right">
              <UserSelectInput
                v-model="form.delegateTaskUserId"
                v-bind:inputValue="form.delegateTaskUserId"
                v-bind:roleName="ecmCfgActivity.roleName"
                :isRepeat="ecmCfgActivity.isMulti"
              ></UserSelectInput>
            </el-col>
          </el-row>
          <el-row></el-row>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">{{$t('application.cancel')}}</el-button>
        <el-button
          v-if="ecmCfgActivity.enableDelegate==true"
          @click="showOrHiddenDelegate()"
        >{{delegateButton}}</el-button>
        <el-button @click="claim(currentData)">认领任务</el-button>
        <el-button @click="completetask(form)">完成任务</el-button>
      </div>
    </el-dialog>
    <el-row>
      <el-form ref="workflowForm" :model="workflowForm">
        <el-row class="topbar-button">
          <el-form-item label="流程名称" :label-width="formLabelWidth" style="float:left">
              <el-select v-model="workflowForm.workflowName" @change="changeJobNames()">
                <div v-for="item in workflowNames" :key="item.id" >
                  <el-option :label="item.name" :value="item.id"></el-option>
                </div>
              </el-select>
          </el-form-item>
          <el-form-item label="任务名称" :label-width="formLabelWidth" style="float:left">
              <el-select v-model="workflowForm.jobName">
                <div v-for="item in jobNames" :key="item.id" >
                  <el-option :label="item.activityName" :value="item.activityName"></el-option>
                </div>
              </el-select>
          </el-form-item>
          <el-form-item label="到达时间" :label-width="formLabelWidth" style="float:left">
              <el-date-picker
                v-model="workflowForm.startTimeAfter"
                auto-complete="off"
                value-format="yyyy-MM-dd HH:mm:ss"
              ></el-date-picker>
              <el-date-picker
                v-model="workflowForm.startTimeBefore"
                auto-complete="off"
                value-format="yyyy-MM-dd HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
            <el-form-item style="float:left;padding-left:3px">
              <el-button type="primary" :plain="true" size="small" @click="search()">查询</el-button>
            </el-form-item>
        </el-row>
      </el-form>
    </el-row>
    <el-table
      :data="dataList"
      border
      :height="tableHeight"
      v-loading="loading"
      @selection-change="selectChange"
      style="width: 99.8%"
    >
      <el-table-column type="selection" width="40"></el-table-column>
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column prop="name" label="任务名称" min-width="15%" sortable></el-table-column>
      <el-table-column prop="formId" label="表单Id" v-if="1==2" min-width="15%" sortable></el-table-column>
      <el-table-column prop="startUser" label="发起人" min-width="15%" sortable></el-table-column>
      <el-table-column
        prop="createTime"
        label="到达时间"
        :formatter="dateFormatter"
        min-width="10%"
        sortable
      ></el-table-column>
      <el-table-column label="操作" width="80">
        <template slot-scope="scope">
          <el-button
            :plain="true"
            type="success"
            size="small"
            icon="save"
            @click="showitem(scope.row)"
          >查看</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[20, 50, 100, 200]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="itemCount"
    ></el-pagination>
  </div>
</template>

<script type="text/javascript">
// $.ajaxSetup({
//   contentType: "application/json"
// });
import UserSelectInput from "@/components/controls/UserSelectInput";
import TaskTestForm1 from "@/components/form/TaskTestForm1.vue";
import EditTask from "@/views/workflow/task/EditTask.vue";
import DocViewTask from "@/views/workflow/task/DocViewTask.vue";
import borrow1 from "@/components/form/Borrow1.vue";
import CommonView from "@/views/workflow/CommonView.vue";
import CommonViewRelyDocType from "@/views/workflow/CommonViewRelyDocType.vue";
import CommonViewRelyFolder from "@/views/workflow/CommonViewRelyFolder.vue";
import DeliverFormTask from "@/views/workflow/DeliverFormTask.vue"
import UpdateDocContent from "@/views/workflow/LinkMainAttachmentFile.vue";
import UpdateDocContentByReviewer from "@/views/workflow/LinkMainAttachmentFileByReviewer.vue";
import BorrowView from "@/views/workflow/BorrowView.vue"
import BorrowViewReadOnly from "@/views/workflow/BorrowViewReadOnly.vue"
import CancelView from "@/views/workflow/CancelView.vue"
import CancelViewReadOnly from "@/views/workflow/CancelViewReadOnly.vue"
import DesignCancelViewReadOnly from "@/views/workflow/DesignCancelViewReadOnly.vue"
import DesignCancelView from "@/views/workflow/DesignCancelView.vue"
import ViewDocNpc from "@/views/npc/ViewDocNpc.vue"
import AppraisalView from "@/views/workflow/AppraisalView.vue"
import AppraisalViewReadOnly from "@/views/workflow/AppraisalViewReadOnly.vue"
export default {
  name: "TodoTask",
  permit: 1,
  components: {
    UserSelectInput: UserSelectInput,
    TaskTestForm1: TaskTestForm1,
    EditTask: EditTask,
    DocViewTask: DocViewTask,
    borrow1: borrow1,
    CommonView: CommonView,
    UpdateDocContent: UpdateDocContent,
    UpdateDocContentByReviewer: UpdateDocContentByReviewer,
    DeliverFormTask:DeliverFormTask,
    BorrowViewReadOnly:BorrowViewReadOnly,
    BorrowView:BorrowView,
    CommonViewRelyDocType:CommonViewRelyDocType,
    CommonViewRelyFolder: CommonViewRelyFolder,
    CancelView:CancelView,
    CancelViewReadOnly : CancelViewReadOnly,
    DesignCancelView:DesignCancelView,
    DesignCancelViewReadOnly:DesignCancelViewReadOnly,
    ViewDocNpc:ViewDocNpc,
    AppraisalViewReadOnly:AppraisalViewReadOnly,
    AppraisalView:AppraisalView
  },
  data() {
    return {
      workflowNames:[],
      jobNames:[],
      sequenceName:'',
      currentData: [],
      taskName: "",
      taskTableData: [],
      dataList: [],
      dataListFull: [],
      inputkey: "",
      pageSize: 20,
      itemCount: 0,
      selectedItems: [],
      currentPage: 1,
      loading: false,
      dialogVisible: false,
      dialogTitle: "查看任务",
      isCompleteSelected: false,
      tableHeight: window.innerHeight - 120,
      delegateDialogVisible: false,
      delegateButton: "委托代理",
      ecmCfgActivity: [],
      result: "通过",
      form: {
        taskId: 0,
        result: "通过",
        message: "",
        delegateTaskUserId: ""
      },
      taskForm: {},
      formData: {},
      formLabelWidth: "120px",
      taskList: [],
      formEditPermision: 0,
      rejectButton: "驳回",
      allowEdit: false,
      sequenceFlow: [],
      isShowReject:false,
      formParameter:{},
      workflowForm: {
        workflowName: "",
        jobName:"",
        startTimeAfter:"",
        startTimeBefore:""
      }
    };
  },
  created() {
    let _self = this;
    var psize = localStorage.getItem("taskPageSize");
    if (psize) {
      _self.pageSize = parseInt(psize);
    }
    _self.loadWorkflowInfo();
    _self.refreshData();
  },
  methods: {
    click(value) {
      if(value.get("metaData")){
        this.formData = value.get("metaData");
      }else{
        this.formData=value;
      }
      
      this.taskForm = value.get("metaData");
      console.log(this.taskForm)
    },
    refreshData() {
      let _self = this;
      _self.selectedItems = [];
      _self.loading = true;
      var m = new Map();
      m.set("workflowForm", _self.workflowForm);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", (_self.currentPage - 1) * _self.pageSize);
      m.set("userId", sessionStorage.getItem("access-userName"));
      axios
        .post("/workflow/todoTask", JSON.stringify(m))
        .then(function(response) {
          _self.dataList = response.data.data;
          _self.dataListFull = response.data.data;
          if (
            _self.$route.query.openTaskFromMainPage == "1" &&
            _self.$route.query.taskId != ""
          ) {
            for (let index = 0; index < _self.dataList.length; index++) {
              if (_self.dataList[index].id == _self.$route.query.taskId) {
                _self.showitem(_self.dataList[index]);
                break;
              }
            }
          }

          _self.loading = false;
          _self.loadPageInfo(response.data.totalCount);
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    // 表格行选择
    selectChange(val) {
      this.selectedItems = val;
      console.log(val);
    },
    // 分页 页数改变
    handleSizeChange(val) {
      this.pageSize = val;
      localStorage.setItem("taskPageSize", val);
      this.refreshData();
      //console.log('handleSizeChange', val);
    },
    // 分页 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val;
      this.refreshData();
    },
    // 加载页数 暂时未处理查询条件
    loadPageInfo(val) {
      let _self = this;
      _self.itemCount = val;
      _self.loading = false;
    },
    getFormdataMap() {
      let _self = this;
      return _self.formData;
    },
    completetask(indata) {
      let _self = this;
      if (_self.isCompleteSelected) {
        _self.form.taskId = [];
        _self.form.formId = [];
        for (let i = 0; i < _self.selectedItems.length; i++) {
          _self.form.taskId[i] = _self.selectedItems[i].id;
        }
      }
      // let docMap = _self.getFormdataMap();
      _self.loading = true;
      // if (_self.formEditPermision == 1) {
      //   switch (_self.currentData.processDefinitionId.split(":")[0]) {
      //     case "BianJiaoShenPi":
      //       _self.$refs.propertiesComp.$refs.ShowProperty.saveItem();
      //       break;
      //     case "process_borrow":
      //       _self.$refs.propertiesComp.saveCurrentForm();
      //       break;
      //   }
        // if(_self.currentData.processDefinitionId.split(":")[0]=="BianJiaoShenPi"){
          try{
            
            _self.$refs.propertiesComp.sendData();
          }catch(e){
            _self.completetaskFinal(_self);
            return;
          }
        let docMap = _self.getFormdataMap();
        axios
          .post("/dc/saveDocument", docMap)
          .then(function(response) {
            _self.completetaskFinal(_self);
          })
          .catch(function(error) {
            console.log(error);
            _self.loading = false;
          });
      // } else {
      //   _self.completetaskFinal(_self);
      // }
    },
    completetaskFinal(indata) {
      let _self = indata;
      _self.form["C_ITEM_STATUS"] = _self.form.result;
      axios
        .post("/workflow/completeTask", JSON.stringify(_self.form))
        .then(function(response) {
          _self.dialogVisible = false;
          _self.refreshData();
          _self.$message("完成任务成功!");
          _self.$emit("refreshcount");
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    delegateTask(indata) {
      let _self = this;
      _self.delegateDialogVisible = true;
      _self.loading = true;
      axios
        .post("/workflow/delegateTask", JSON.stringify(indata))
        .then(function(response) {
          _self.loading = false;
          _self.dialogVisible = false;
          _self.refreshData();
          _self.$message("委托代理成功!");
          _self.$emit("refreshcount");
        })
        .catch(function(error) {
          _self.loading = false;
          console.log(error);
        });
    },
    claim(indata) {
      let _self = this;
      _self.loading = true;
      axios
        .post("/workflow/claim", JSON.stringify(indata))
        .then(function(response) {
          _self.loading = false;
          _self.$message("认领成功!");
        })
        .catch(function(error) {
          _self.loading = false;
          console.log(error);
        });
    },

    showOrHiddenDelegate() {
      let _self = this;
      if (_self.delegateDialogVisible == false) {
        _self.delegateButton = "取消委托";
        _self.delegateDialogVisible = true;
      } else {
        _self.delegateButton = "委托代理";
        _self.delegateDialogVisible = false;
      }
    },

    completeselected() {
      let _self = this;
      if (_self.selectedItems.length == 0) {
        _self.$message("未选择任务!");
      } else {
        _self.dialogTitle = "批量完成任务";
        _self.isCompleteSelected = true;
        _self.dialogVisible = true;
      }
    },
    showitem(indata) {
      let _self = this;
      _self.dialogTitle = "查看任务";
      _self.delegateDialogVisible = false;
      _self.isCompleteSelected = false;
      _self.currentData = indata;
      _self.form.taskId = indata.id;
      _self.form.formId = indata.formId;
      _self.dialogVisible = true;
      _self.taskTableData = [];
      // _self.sequenceName = _self.currentData.processDefinitionId.split(":")[0]
      //  if ("借阅驳回" == indata.name) {
      //     _self.rejectButton = "结束";
      //   } else {
      //     // _self.formEditPermision = 0;
      //     _self.rejectButton = "驳回";
      //   }

      _self.$nextTick(() => {
        //动态获取表单 显示查看或编辑页面
        var m = new Map();
        m.set("processDefinitionId", indata.processDefinitionId);
        m.set("activityName", indata.name);
        m.set("taskId",indata.id);
        axios
          .post("/workflow/getEcmCfgActivity", JSON.stringify(m))
          .then(function(response) {
            _self.ecmCfgActivity = response.data.data;
            if(_self.ecmCfgActivity.formParameter){
              _self.formParameter= JSON.parse(_self.ecmCfgActivity.formParameter) 
            }
            _self.taskName = _self.ecmCfgActivity.componentName;
            _self.rejectButton = _self.ecmCfgActivity.rejectActivityLabel;
            _self.allowEdit = _self.ecmCfgActivity.enableEdit;
            _self.sequenceFlow=response.data.sequenceNames;
            if(_self.sequenceFlow.indexOf('驳回')>-1){
              _self.isShowReject=true;
            }else{
              _self.isShowReject=false;
            }
            if (_self.allowEdit === true) {
              _self.formEditPermision = 1;
            } else {
              _self.formEditPermision = 0;
            }

            axios
              .post("/dc/getDocumentById", indata.formId)
              .then(function(responsedoc) {
                let result = responsedoc.data;
                if (result.code == 1) {
                  _self.formData = result.data;
                }
              });
            axios
              .post("/dc/getFormRelateDocument", indata.formId)
              .then(function(responseRdoc) {
                let result = responseRdoc.data;
                if (result.code == 1) {
                  _self.tabledata = result.data;
                }
              });
            var m = new Map();
            m.set("processInstanceId", indata.processInstanceId);
            axios
              .post("/workflow/getWorkflowTask", JSON.stringify(m))
              .then(function(response) {
                _self.taskList = response.data.data;
                //console.log(JSON.stringify(_self.taskList));
                _self.dialogVisible = true;
                _self.loading = false;
              })
              .catch(function(error) {
                console.log(error);
                _self.loading = false;
              });
          })
          .catch(function(error) {
            console.log(error);
            _self.loading = false;
          });
      });
    },
    loadGridView() {
      let _self = this;
      var m = new Map();
      m.set("gridName", _self.gridviewName);
      m.set("lang", _self.currentLanguage);
      axios
        .post("/dc/getGridViewInfo", JSON.stringify(m))
        .then(function(response) {
          _self.tabledata = _self.$route.query.tabledata;
          //_self.loadData();
        });
    },
    loadData() {
      let _self = this;
      axios.post("/dc/getRelations", this.docId).then(function(response) {
        let result = response.data;

        if (result.code == 1) {
          _self.tabledata = result.data;
        }
      });
    },
    dateFormatter(row, column) {
      let datetime = row[column.property];
      return this.datetimeFormat(datetime);
    },
    search() {
      let _self = this;
      _self.refreshData();
    },
    loadWorkflowInfo(){
      let _self = this
      axios.get("/cfgworkflow/processes").then(function(response) {
        _self.workflowNames = response.data.data
      });
    },
    changeJobNames(){
      let _self = this;
      _self.workflowForm.jobName = ""
      var jobNamesArr = new Array();
      axios
        .get("/cfgworkflow/cfgActivities/"+_self.workflowForm.workflowName)
        .then(function(response) {
        jobNamesArr = response.data.data
        jobNamesArr.forEach(function(value,index){
          if(value.activityName == "start"){
            jobNamesArr.splice(index, 1)
          }
        })
        _self.jobNames = jobNamesArr
        })
        .catch(function(error) {
          console.log(error);
        });
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
.el-header {
  background-color: #e8eaeb;
  height: 28px !important;
}
.el-row {
  padding-bottom: 10px;
}
</style>
