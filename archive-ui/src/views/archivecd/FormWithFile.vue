<template>
  <DataLayout>
    <template v-slot:header>
        <el-dialog :title="'创建'+childType" 
        :visible.sync="createFileVisible" 
        @close="createFileVisible = false"
        width="80%" :append-to-body="true">
            <CreateDocFile ref="createDocFile" :fileType="childType" :selectedItemId="docId" @saveOrstartSuccess="closeCreateDialog"></CreateDocFile>
            <div slot="footer" class="dialog-footer">
                <!-- <el-button type="primary" @click="saveOrstart(true)">发起流程</el-button> -->
                <el-button type="success" @click="saveOrstart(false)">保存</el-button>
                <el-button @click="closeCreateDialog" size="medium">{{$t('application.close')}}</el-button>
            </div>
        </el-dialog>
    </template>
    <template v-slot:main>
      
      <el-card shadow="hover">
        <el-row>
          <el-col :span="24">
            <ShowProperty
              ref="ShowProperty"
              @onSaved="onSaved"
              @onSaveSuccess="onPropertiesSaveSuccess"
              width="95%"
              :typeName="fileType"
              :showUploadFile="false"
              :extendAllTab="false"
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
            
          </el-collapse-item>
          
        </el-collapse>
      </el-card>
      <template v-if="childType!=''">
      <el-card shadow="hover">
        <el-collapse value="文件" accordion>
          <el-collapse-item
            title="文件"
            name="文件"
            id="formFile"
            key="formFile"
          >
            
            <el-row v-if="allowEdit">
              <el-col :span="24" style="text-align: left">
                <el-form :inline="true" :model="filters" @submit.native.prevent>
                  <template v-if="allowEdit">
                    <el-form-item>
                      <el-button type="primary" @click="beforeNewDocument">{{ $t("application.new") }}</el-button>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="danger" @click="onDeleleItem(selectedArchives,[$refs.fileList])">{{ $t("application.delete") }}</el-button>
                    </el-form-item>
                    <el-form-item>
                      <MountFile
                        :selectedItem="selectedArchives"
                        :title="$t('application.ReplaceDoc')"
                      >{{$t('application.replace')}}</MountFile>
                    </el-form-item>
                  </template>
                  
                </el-form>
              </el-col>
            </el-row>
            
            <DataGrid
              ref="fileList"
              key="fileList"
              :parentId="selectedItemId"

              dataUrl="/dc/getDocuByRelationParentId"
              gridViewName='ArrangeInnerGrid'
              condition="and a.NAME='irel_children'"
              
              v-bind:tableHeight="tableHeight"
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              
              :optionWidth="1"
              :isshowCustom="false"
              :isShowMoreOption="true"
              showOptions="查看内容,查看属性"
              :isShowPropertyButton="false"
              :isEditProperty="allowEdit"
              :isShowChangeList="false"
              :isshowicon="false"
              :isshowPage="isShowPage"
              @selectchange="archiveSelect"
              @dbclick="showVolumesFile"
            >
              <template slot="sequee" slot-scope="scope">
                <span
                  :style="(scope.data.row['C_REJECT_COMMENT']!=null
                      &&scope.data.row['C_REJECT_COMMENT']!='')?{'background':'red'}:''"
                >{{(scope.currentPage-1) * scope.pageSize+ scope.data.$index+1}}</span>
              </template>
            </DataGrid>
          </el-collapse-item>
          
        </el-collapse>
      </el-card>
      </template>
      <el-card shadow="hover">
        <el-collapse value="选择流程审批人员">
          <el-collapse-item
            title="选择流程审批人员"
            name="选择流程审批人员"
            id="selectApprover"
            key="cindex"
          >
            <el-row>
              <el-form ref="taskForm" :model="taskForm">
                <div
                  v-for="(approver, index) in approvalUserList"
                  :key="'approver_' + index"
                >
                  <!-- <label>{{'approver_'+index}}</label> -->
                  <el-form-item
                    :label="approver.activityName.split(':')[0]"
                    :rules="[{required:validateValue(approver.activityName),message:$t('application.requiredInput'),trigger:['blur','change']}]"
                    :label-width="formLabelWidth"
                    :prop="approver.performerPolicy"
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
      
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import ShowProperty from "@/components/ShowProperty";
import UserSelectInput from "@/components/controls/UserSelectInput";
import CreateDocFile from "@/views/archivecd/CreateDocFile.vue";
import DataGrid from "@/components/DataGrid";
import MountFile from "@/components/MountFile.vue";
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
      createFileVisible:false,
      selectedArchives:[]
    };
  },
  props: {
    selectedItemId: { type: String, default: "" },
    fileType:{type:String,default:""},
    childType:{type:String,default:""},
    templateDoc:{type:Object,default:null},
    allowEdit:{type:Boolean,default:false},
    isShowReject:{type:Boolean,default:false}
  },
  mounted() {
    this.getApprovalUserList();
    
  },
  components: {
    DataLayout: DataLayout,
    ShowProperty: ShowProperty,
    UserSelectInput: UserSelectInput,
    CreateDocFile:CreateDocFile,
    DataGrid:DataGrid,
    MountFile:MountFile
  },
  methods: {
      archiveSelect(val) {
        this.selectedArchives = val;
        },
      closeCreateDialog(){
            this.createFileVisible=false;
        },
      saveOrstart(isStart){
            this.$refs.createDocFile.saveOrStartup(isStart);
        },
      beforeNewDocument(){
            
            let _self=this;
            let m=new Map();
            m.set("typeName",_self.childType);
            m.set("templateId",_self.templateDoc.id);
            axios
            .post("/cd/dc/newDocumentWithTemplate", JSON.stringify(m))
            .then(function (response) {
                if(response.data.code=='1'){
                    _self.docId=response.data.id;
                    _self.createFileVisible=true;
                }else{
                    _self.$message({
                        showClose: true,
                        message: response.data.message,
                        duration: 2000,
                        type: 'error'
                    });
                }
                
            })
            .catch(function (error) {
                console.log(error);
            });
        },
      
    validateValue(itemData){
      if(itemData.split(':')[1]=='true'){
        return true;
      }
      return false;
    },
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
       this.reload();
    },
    
    validateApprover(){
      let result=false;
       this.$refs.taskForm.validate((valid) => {
          if (valid) {
            result=true;
            return true;
          } else {
            console.log('error submit!!');
            result=false
            return false;
          }
        });
        return result;
    },
    saveOrStartup(isStartup) {
      let _self = this;
      if(!_self.$refs.ShowProperty.validFormValue()){
        return;
      }
      let x= _self.validateApprover();
      if(!x){
        return;
      }
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
      m.push(["C_PROCESS_DEF_NAME","图纸文件审批流程"]);
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
            // _self.$emit("onSaved", "new");
            // _self.$emit("onSaveSuccess", param);
            if(!isStartup){
                _self.$message({
                  showClose: true,
                  message: "保存成功",
                  duration: 2000,
                  type: "success"
                });
                _self.butt=false;
                _self.loading = false;
                _self.$emit("saveOrstartSuccess");
            }
            if(isStartup){
                _self.startworkflow(param);
            }else{
              // _self.reload();
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
        _self.$emit("saveOrstartSuccess");
        // _self.reload();
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
      // m.set("processDefinitionName", "图纸文件审批流程");
      ///workflow/getApprovalAllUserList
      
      m.set("processName", "图纸文件审批流程");
      m.set("activityName", "start");
      axios
        .post("/workflow/getApprovalUserListVisible", JSON.stringify(m))
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
