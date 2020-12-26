<template>
  <div>
    <el-dialog title="查看" :visible.sync="dialogVisible" v-if="dialogVisible" width="95%">
      <el-divider content-position="left">表单信息</el-divider>
      <!-- <router-view ref="formRouter"></router-view> -->
      <component
        :is="taskName"
        :typeName="taskName"
        v-model="formData"
        :formId="currentFormId"
        :docId="currentFormId"
        :istask="1"
        :formParameter="formParameter"
        :allowEdit="false"
        :isTodoTask="false"
        :processDefinitionId="currentData.processDefinitionId"
        :activityName="currentData.name"
        :formEditPermision=0
        :formEnableType="this.$options.name"
        :allowChangeDoc="false"
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
      <div v-if="workflowPicVisible=='显示流程图'">
        {{workflowPicVisible}}
        <img
          style="width:100%;height:100%"
          :src="this.axios.defaults.baseURL+'/workflow/processDiagram?processId='+currentProcessId"
        />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">{{$t('application.close')}}</el-button>
        <el-button @click="showprocessDiagram()">显示流程图</el-button>
      </div>
    </el-dialog>
    <el-row>
      <el-form ref="workflowForm" :model="workflowForm">
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
          <el-form-item label="开始时间" :label-width="formLabelWidth" style="float:left">
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
      </el-form>
    </el-row>
    <el-table
      :data="dataList"
      border
      :height="tableHeight"
      v-loading="loading"
      style="width: 99.8%"
    >
      <el-table-column type="index" width="50"></el-table-column>
      <el-table-column prop="name" label="名称" min-width="20%" sortable></el-table-column>
      <el-table-column prop="startUser" label="发起人" min-width="12%" sortable></el-table-column>
      <el-table-column
        prop="createTime"
        label="开始时间"
        :formatter="dateFormatter"
        min-width="12%"
        sortable
      ></el-table-column>
      <el-table-column
        prop="endTime"
        label="完成时间"
        :formatter="dateFormatter"
        min-width="12%"
        sortable
      ></el-table-column>
      <!-- <el-table-column prop="result" label="完成结果"  min-width="10%">
                </el-table-column>
                <el-table-column prop="message" label="审批意见"  min-width="15%">
      </el-table-column>-->
      <el-table-column label="操作" width="80">
        <template slot-scope="scope">
          <el-button
            :plain= true
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
import DeliverFormTask from "@/views/workflow/DeliverFormTask.vue"
import UpdateDocContent from "@/views/workflow/LinkMainAttachmentFile.vue";
import UpdateDocContentByReviewer from "@/views/workflow/LinkMainAttachmentFileByReviewer.vue";
import BorrowView from "@/views/workflow/BorrowView.vue"
import BorrowViewReadOnly from "@/views/workflow/BorrowViewReadOnly.vue"
import CancelView from "@/views/workflow/CancelView.vue"
import CancelViewReadOnly from "@/views/workflow/CancelViewReadOnly.vue"
import CommonViewRelyFolder from "@/views/workflow/CommonViewRelyFolder.vue";
import ViewDocNpc from "@/views/npc/ViewDocNpc.vue"
export default {
  name: "DoneTask",
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
    CancelView:CancelView,
    CancelViewReadOnly : CancelViewReadOnly,
    CommonViewRelyFolder : CommonViewRelyFolder,
    ViewDocNpc:ViewDocNpc
  },
  data() {
    return {
      workflowNames:[],
      jobNames:[],
      currentData: [],
      ecmCfgActivity: [],
      taskName: "",
      dataList: [],
      dataListFull: [],
      taskList: [],
      inputkey: "",
      pageSize: 20,
      itemCount: 0,
      selectedItems: [],
      currentPage: 1,
      loading: false,
      dialogVisible: false,
      tableHeight: window.innerHeight - 170,
      formLabelWidth: "120px",
      processDiagram: "",
      currentProcessId: "",
      workflowPicVisible: "",
      isPocessFinished: "0",
      currentFormId: "",
      taskForm: {},
      formData: {},
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
    dateFormatter(row, column) {
      let datetime = row[column.property];
      return this.datetimeFormat(datetime);
    },
    refreshData() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("workflowForm", _self.workflowForm);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", (_self.currentPage - 1) * _self.pageSize);
      m.set("userId", sessionStorage.getItem("access-userName"));
      axios
        .post("/workflow/doneTask", JSON.stringify(m))
        .then(function (response) {
          _self.dataList = response.data.data;
          _self.dataListFull = response.data.data;
          _self.loading = false;
          _self.loadPageInfo(response.data.totalCount);
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    // 表格行选择
    selectChange(val) {
      this.selectedItems = val;
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
    showitem(indata) {
      let _self = this;
      var m = new Map();
      var n = new Map();
      n.set("processDefinitionId", indata.processDefinitionId);
      n.set("activityName", indata.name);
      n.set("taskId",indata.id);
      axios
        .post("/workflow/getEcmCfgActivity", JSON.stringify(n))
        .then(function(response){
          _self.ecmCfgActivity = response.data.data;
          if(_self.ecmCfgActivity.formParameter){
              _self.formParameter= JSON.parse(_self.ecmCfgActivity.formParameter) 
            }
          _self.taskName = _self.ecmCfgActivity.componentName;
          axios
              .post("/dc/getDocumentById", indata.formId)
              .then(function(responsedoc) {
                let result = responsedoc.data;
                if (result.code == 1) {
                  _self.formData = result.data;
                }
              });
        }).catch(function(error) {
          console.log(error);
          _self.loading = false;
        });

      _self.currentFormId = indata.formId;
      _self.currentData = indata;
      _self.currentProcessId = indata.processInstanceId;
      m.set("processInstanceId", indata.processInstanceId);
      axios
        .post("/workflow/getWorkflowTask", JSON.stringify(m))
        .then(function (response) {
          _self.taskList = response.data.data;
          _self.isPocessFinished = response.data.isPocessFinished;
          if (_self.isPocessFinished == "1") {
            _self.workflowPicVisible = "";
          }
          _self.dialogVisible = true;
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    search() {
      let _self = this;
      _self.refreshData();
    },
    showprocessDiagram() {
      let _self = this;
      if (_self.isPocessFinished == "1") {
        _self.workflowPicVisible = "";
        _self.$message({
          showClose: true,
          message: "流程已结束，过程流程图不可查看!",
          duration: 2000,
          type: "success",
        });
      } else {
        if (_self.workflowPicVisible == "显示流程图") {
          _self.workflowPicVisible = "";
        } else {
          _self.workflowPicVisible = "显示流程图";
        }
      }
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
  },
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
