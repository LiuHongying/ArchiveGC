<template>
  <DataLayout>
    <template v-slot:header></template>
    <template v-slot:main>
      <el-card>
        <el-collapse value="模板" accordion>
          <el-collapse-item
            title="模板"
            name="模板"
            id="selectTemplate"
            key="cindex"
          >
            <el-row>
              <el-col :span="3" class="el-form-item__content">模板1</el-col>
              <el-col :span="6" class="el-form-item__content">
                <el-button type="primary">预览</el-button>
                <el-button type="primary">下载</el-button>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-card>
      <el-card>
        <el-row>
          <el-col :span="24">
            <ShowProperty
              ref="ShowProperty"
              @onSaved="onSaved"
              @onSaveSuccess="onPropertiesSaveSuccess"
              width="95%"
              typeName="图纸文件审批单"
              :showUploadFile="false"
              v-bind:itemId="selectedItemId"
            ></ShowProperty>
          </el-col>
        </el-row>
      </el-card>
      <el-card>
          <el-collapse value="上传文件" accordion>
            <el-collapse-item
              title="上传文件"
              name="上传文件"
              id="uploadFile"
              key="cindex"
            >
              <el-row style="margin:10px">
                <el-col :span="3">上传主件：</el-col>
                <el-col :span="20">
                  <el-upload
                    :limit="1"
                    :file-list="fileList"
                    action
                    :on-change="handleChange"
                    :auto-upload="false"
                    :multiple="false"
                  >
                    <el-button slot="trigger" size="small" type="primary">{{
                      $t("application.selectFile")
                    }}</el-button>
                  </el-upload>
                </el-col>
              </el-row>
              <el-row style="margin:10px">
                <el-col :span="3">上传附件：</el-col>
                <el-col :span="20">
                  <el-upload
                    :limit="0"
                    :file-list="fileAttachList"
                    action
                    :on-change="handleChangeAttach"
                    :auto-upload="false"
                    :multiple="true"
                    :on-preview="showItemContent"
                    :before-remove="beforRemoveAttach"
                  >
                    <el-button slot="trigger" size="small" type="primary">{{
                      $t("application.selectFile")
                    }}</el-button>
                  </el-upload>
                </el-col>
              </el-row>
            </el-collapse-item>
            <!-- <el-collapse-item
            title="启动流程"
            name="startupWorkflow"
            id="startupWorkflow"
            key="cindexstartupWorkflow"
          >
          </el-collapse-item> -->
          
        </el-collapse>
        <el-row>
            <el-divider content-position="left">选择流程审批人员</el-divider>
            <el-form>
            <div v-for="(approver,index)  in approvalUserList" :key="'approver_'+index">
                <!-- <label>{{'approver_'+index}}</label> -->
                <el-form-item :label="approver.activityName"  :label-width="formLabelWidth" style="float:left">
                <UserSelectInput
                    v-model="taskForm[approver.performerPolicy]"
                    v-bind:inputValue="taskForm[approver.performerPolicy]"
                    v-bind:roleName="approver.roleName"
                    
                ></UserSelectInput>
                <!-- :buttonType = "formEnableType != 'TodoTask'" -->
                </el-form-item>
            </div>
            </el-form>
        </el-row>
      </el-card>
      <el-card>
        <el-collapse value="选择流程审批人员">
          <el-collapse-item
            title="选择流程审批人员"
            name="选择流程审批人员"
            id="selectApprover"
            key="cindex"
          >
            <el-row>
              <el-form>
                <div
                  v-for="(approver, index) in approvalUserList"
                  :key="'approver_' + index"
                >
                  <!-- <label>{{'approver_'+index}}</label> -->
                  <el-form-item
                    :label="approver.activityName"
                    :label-width="formLabelWidth"
                    style="float:left"
                  >
                    <UserSelectInput
                      v-model="taskForm[approver.formAttribute]"
                      v-bind:inputValue="taskForm[approver.formAttribute]"
                      v-bind:roleName="approver.roleName"
                    ></UserSelectInput>
                    <!-- :buttonType = "formEnableType != 'TodoTask'" -->
                  </el-form-item>
                </div>
              </el-form>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-card>
      <el-card>
         <el-collapse value="操作">
          <el-collapse-item
            title="操作"
            name="操作"
            id="action"
            key="cindex"
          >
       <el-row>
          <el-col :span="24">
            <el-button type="primary" @click="getData()">发起流程</el-button>
            <el-button>暂 存</el-button>
          </el-col>
        </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import ShowProperty from "@/components/ShowProperty";
import UserSelectInput from "@/components/controls/UserSelectInput";
export default {
  name: "CreateDoc",
  data() {
    return {
      file: [],
      fileList: [],
      uploadFileModel: "",
      fileAttachList: [],
      approvalUserList: [],
      taskForm: {},
      formLabelWidth: "120px"
    };
  },
  props: {
    selectedItemId: { type: String, default: "" }
  },
  mounted() {
    this.getApprovalUserList();
    this.fileAttachList = [
      {
        name: "food.jpg",
        id: "cb82ba8119914da9a6f219ba4c0b0be4",
        url: "https://xxx.cdn.com/xxx.jpg"
      }
    ];
  },
  components: {
    DataLayout: DataLayout,
    ShowProperty: ShowProperty,
    UserSelectInput: UserSelectInput
  },
  methods: {
    // 查看内容
    showItemContent(indata) {
      if (!indata.id) {
        return;
      }
      let condition = indata.id;
      let href = this.$router.resolve({
        path: "/viewdoc",
        query: {
          id: condition
          //token: sessionStorage.getItem('access-token')
        }
      });
      //console.log(href);
      window.open(href.href, "_blank");
    },
      beforRemoveAttach(file,fileList){
          
      },
      getData(){
          let _self=this;
          let formData= _self.$refs.ShowProperty.getFormData();
          let jsonStr= formData.get('metaData');
          let m= JSON.parse(jsonStr);
          for(let key in _self.taskForm){
              let p=new Array();
              p.push(key);
              p.push(_self.taskForm[key]);
              m.push(p);
          }
        let formdataNew = new FormData();
        formdataNew.append("metaData",JSON.stringify(m));
        if(_self.file!="")
        {
            //console.log(_self.file);
            formdataNew.append("mainFile",_self.file.raw);
            
        }
        if(_self.fileAttachList!=""){
            let attachs=new Array();
            for(let i=0;i<_self.fileAttachList.length;i++){
                // attachs.push(_self.fileAttachList[i].raw)
                formdataNew.append("attachFiles",_self.fileAttachList[i].raw);
            }
            
        }
        axios.post("/dc/newDocumentMoreFile",formdataNew,{
            'Content-Type': 'multipart/form-data'
          })
        .then(function(response) {
          let code = response.data.code;
          //console.log(JSON.stringify(response));
          if(code==1){
            _self.$emit('onSaved','new');
            _self.$emit("onSaveSuccess",m);
             _self.$message({
              showClose: true,
              message: "保存成功",
              duration: 2000,
              type: "Success",
            });
          }
          else{
             _self.$message(_self.$t('message.newFailured'));
          }
        })
        .catch(function(error) {
          _self.$message(_self.$t("message.newFailured"));
          console.log(error);
        });
    },
    handleChange(file, fileList) {
      this.file = file;
      //console.log(file);
      // console.log(fileList);
    },
    handleChangeAttach(file, fileList) {
      debugger;
      this.fileAttachList = fileList;
      //console.log(file);
      // console.log(fileList);
    },
    getApprovalUserList() {
      let _self = this;
      var m = new Map();
      m.set("processDefinitionName", "图纸文件审批流程");
      axios
        .post("/workflow/getApprovalAllUserList", JSON.stringify(m))
        .then(function(response) {
          if (response.data.code == 1) {
            _self.approvalUserList = response.data.data;
          } else {
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "warning"
            });
            return;
          }
        });
    }
  }
};
</script>
<style scoped></style>
