<template>
  <DataLayout>
    <template v-slot:header></template>
    <template v-slot:main>
      <!-- <el-card shadow="hover">
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
      </el-card> -->
      <el-card shadow="hover">
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
      <el-card shadow="hover">
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
      </el-card>
      <el-card shadow="hover">
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
                      v-model="taskForm[approver.performerPolicy]"
                      v-bind:inputValue="taskForm[approver.performerPolicy]"
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
      <el-card shadow="hover">
        <el-collapse value="操作">
          <el-collapse-item title="操作" name="操作" id="action" key="cindex">
            <el-row>
              <el-col :span="24">
                <el-button type="primary" :loading="butt" @click="saveOrStartup(true)"
                  >发起流程</el-button
                >
                <el-button type="primary" :loading="butt" @click="saveOrStartup(false)">暂 存</el-button>
                <!-- <el-button type="primary" :loading="butt" @click="beforRemoveAttach()">刷新</el-button> -->
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
  inject:['reload'],
  data() {
    return {
      file: [],
      fileList: [],
      uploadFileModel: "",
      fileAttachList: [],
      approvalUserList: [],
      taskForm: {},
      formLabelWidth: "120px",
      butt:false,
    };
  },
  props: {
    selectedItemId: { type: String, default: "" }
  },
  mounted() {
    this.getApprovalUserList();
    
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
    beforRemoveAttach() {
        debugger
       this.reload();
    },
    saveOrStartup(isStartup) {
      let _self = this;
      _self.butt=true;
      _self.loading = true;
      let formData = _self.$refs.ShowProperty.getFormData();
      let jsonStr = formData.get("metaData");
      let m = JSON.parse(jsonStr);
      for (let key in _self.taskForm) {
        let p = new Array();
        p.push(key);
        p.push(_self.taskForm[key]);
        m.push(p);
      }
      let formdataNew = new FormData();
      formdataNew.append("metaData", JSON.stringify(m));
      if (_self.file != "") {
        //console.log(_self.file);
        formdataNew.append("mainFile", _self.file.raw);
      }
      if (_self.fileAttachList != "") {
        let attachs = new Array();
        for (let i = 0; i < _self.fileAttachList.length; i++) {
          // attachs.push(_self.fileAttachList[i].raw)
          formdataNew.append("attachFiles", _self.fileAttachList[i].raw);
        }
      }
      axios
        .post("/dc/newDocumentMoreFile", formdataNew, {
          "Content-Type": "multipart/form-data"
        })
        .then(function(response) {
          let code = response.data.code;
          let docId=response.data.id;
          //console.log(JSON.stringify(response));
          if (code == 1) {
              let param=new Map(m);
              param.set("ID",docId);
            _self.$emit("onSaved", "new");
            _self.$emit("onSaveSuccess", param);
            _self.$message({
              showClose: true,
              message: "保存成功",
              duration: 2000,
              type: "success"
            });
            _self.butt=false;
            _self.loading = false;
            if(isStartup){
                _self.startworkflow(param);
            }
          } else {
            _self.$message(_self.$t("message.newFailured"));
            _self.butt=false;
            _self.loading = false;
          }
        })
        .catch(function(error) {
          _self.$message(_self.$t("message.newFailured"));
          console.log(error);
          _self.butt=false;
            _self.loading = false;
        });
    },
    startworkflow(m) {
      let _self = this;
      let form={};
      _self.butt=true;
      _self.loading = true;
      let d=new Map(m);
      form['formId']=d.get('ID');
      form['processInstanceKey']='图纸文件审批流程';
      axios.post('/workflow/startWorkflow',JSON.stringify(form))
      .then(function(response) {
        _self.$message({
              showClose: true,
              message: "流程发起成功",
              duration: 2000,
              type: "success"
            });
        _self.loading = false;
        _self.butt=false;
        _self.reload();
      })
      .catch(function(error) {
        console.log(error);
        _self.loading = false;
        _self.butt=false;
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
