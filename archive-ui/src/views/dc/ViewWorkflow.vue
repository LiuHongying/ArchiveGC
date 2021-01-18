<template>
<div>
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
      
      </div>
</template>

<script>
export default {
  data(){
        return{
          taskList: [],
        }
    },
    props:{
        docId:{
            type:String,
            default:""
        }
    },
    name:"ViewWorkflow",
    created(){
      this.getWorkflowTaskInfo()
    },
    methods:{
       getWorkflowTaskInfo(){
          let _self = this;
          var m = new Map();
          _self.$nextTick(() => {
                  var m = new Map();
                  m.set("docId", this.docId);
                  axios
                    .post("/dc/getWorkflowTaskInfo", JSON.stringify(m))
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
            });
      }
    }
}
</script>

<style>

</style>