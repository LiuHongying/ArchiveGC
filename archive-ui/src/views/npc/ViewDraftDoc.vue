<template>
  <DataLayout>
    <template v-slot:header></template>
    <template v-slot:main="{ layout }">
      <el-row>
        <el-col :span="3" style="height:30px;line-height:30px;text-align:center;">模板名称：</el-col>
        <el-col :span="6" style="height:30px;line-height:30px;text-align:center;">模板名称：</el-col>
        <el-col :span="6">
          <el-button type="primary">预览</el-button>
          <el-button type="primary">下载</el-button>
        </el-col>
      </el-row>
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
      <el-row>
        <el-collapse v-model="uploadFileModel">
          <el-collapse-item
            title="文件列表"
            name="文件列表"
            id="uploadFile"
            key="cindex"
          >
          <el-row>
            <el-button @click="showItemContent(selectedItemId)">查看</el-button>
            <MountFile
                  :selectedItem="[{'ID':selectedItemId}]"
                  >{{ $t("application.ReplaceDoc") }}</MountFile>
          </el-row>
          <el-row>
            <el-button>添加附件</el-button>
            <el-button>删除附件</el-button>
          </el-row>
          <el-row>
            <!--列表-->
              <DataGrid
                  ref="attachmentDoc"
                  key="attachmentDocKey"
                  dataUrl="/dc/getDocuByRelationParentId"
                  v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="AttachmentGrid"
                  condition=" and a.NAME='附件'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="true"
                  showOptions="查看内容"
                  :isShowChangeList="false"
                  :parentId="selectedItemId"
                  :isInitData="true"
                  @selectchange="attachmentDocSelect"
              ></DataGrid>
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
                    v-model="taskForm[approver.formAttribute]"
                    v-bind:inputValue="taskForm[approver.formAttribute]"
                    v-bind:roleName="approver.roleName"
                    
                ></UserSelectInput>
                <!-- :buttonType = "formEnableType != 'TodoTask'" -->
                </el-form-item>
            </div>
            </el-form>
        </el-row>
        <el-row>
            
            <el-col :span="24">
                <el-button type="primary" @click="getData()" style="position:absolute;right:120px;margin:10px;">发起流程</el-button>
                <el-button style="position:absolute;right:50px;margin:10px;" >暂  存</el-button>
            </el-col>
        </el-row>
      </el-row>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import ShowProperty from "@/components/ShowProperty";
import UserSelectInput from "@/components/controls/UserSelectInput";
import DataGrid from "@/components/DataGrid";
import MountFile from "@/components/MountFile.vue";
export default {
  name: "CreateDoc",
  data() {
    return {
      file: [],
      fileList: [],
      uploadFileModel:"",
      fileAttachList:[],
      approvalUserList:[],
      taskForm:{},
      formLabelWidth: "120px",
    };
  },
  props:{
      selectedItemId: {type:String,default:""},
  },
  mounted() {
      this.getApprovalUserList();
      
  },
  components: {
    DataLayout: DataLayout,
    ShowProperty: ShowProperty,
    UserSelectInput:UserSelectInput,
    DataGrid:DataGrid,
    MountFile: MountFile,
  },
  methods: {
      // 查看内容
    showItemContent(id) {
        if(!id){
            return;
        }
      let condition = id;
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
      beforRemoveAttach(file,fileList){
          
      },
      getData(){
          let _self=this;
          let formData= _self.$refs.ShowProperty.getFormData();
          let jsonStr= formData.get('metaData');
          let m= JSON.parse(jsonStr);
          for(let key in _self.taskForm){
              m.set(key,_self.taskForm[key]);
          }
        let formdataNew = new FormData();
        formdataNew.append("metaData",JSON.stringify(m));
        debugger
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
        debugger
        axios.post("/dc/newDocumentMoreFile",formdataNew,{
            'Content-Type': 'multipart/form-data'
          })
        .then(function(response) {
          let code = response.data.code;
          //console.log(JSON.stringify(response));
          if(code==1){
            _self.$emit('onSaved','new');
            _self.$emit("onSaveSuccess",m);
          }
          else{
             _self.$message(_self.$t('message.newFailured'));
          }
        })
        .catch(function(error) {
          _self.$message(_self.$t('message.newFailured'));
          console.log(error);
        });
      },
    handleChange(file, fileList) {
      this.file = file;
      //console.log(file);
      // console.log(fileList);
    },
    handleChangeAttach(file, fileList) {
        debugger
      this.fileAttachList = fileList;
      //console.log(file);
      // console.log(fileList);
    },
    getApprovalUserList(){
        let _self = this;
        var m = new Map();
        m.set("processDefinitionName", "图纸文件审批流程");
        axios
        .post("/workflow/getApprovalAllUserList", JSON.stringify(m))
        .then(function(response) {
            if(response.data.code==1){
                _self.approvalUserList = response.data.data;
            }else{
                _self.$message({
                showClose: true,
                message:response.data.message,
                duration: 2000,
                type: 'warning'
                });
                return;
            }
        });
     
    },
  }
};
</script>
<style scoped>
</style>
