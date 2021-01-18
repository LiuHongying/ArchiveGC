<template>
  <div>
            <el-dialog :title="'创建'+typeName" 
              :visible.sync="createFileVisible" 
              @close="createFileVisible = false"
              width="90%"
            style="width:100%"
            :close-on-click-modal="false"
              v-dialogDrag
              >
              
                  <el-card shadow="hover">
                    <el-row>
                      <el-col :span="24">
                        <ShowProperty
                          ref="ShowProperty"
                          @onSaved="onSaved"
                          @onSaveSuccess="onPropertiesSaveSuccess"
                          width="100%"
                          :typeName="typeName"
                          :showUploadFile="false"
                          :extendAllTab="false"
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
                
              <div slot="footer" class="dialog-footer">
                  <slot name="save">
                    <el-button type="success" :loading="butt" @click="saveOrStartup(false);">保存</el-button>
                  </slot>
                  <slot name="close">
                    <el-button @click="closeCreateDialog" size="medium">{{$t('application.close')}}</el-button>
                  </slot>
              </div>
          </el-dialog>
          <div>
            <el-button type="primary" icon="el-icon-document-add" @click="showDialog">
              <slot>新建</slot>
            </el-button>
          </div>
</div>
  
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
      taskForm: {},
      formLabelWidth: "120px",
      butt:false,
      createFileVisible:false,
    };
  },
  props: {
    typeName:{type:String,default:""},
    parentId:{type:String,default:""},
    folderId:{type:String,default:""},
    url:{type:String,default:"/dc/newDocumentMoreFile"}
  },
  mounted() {
    
    
  },
  components: {
    DataLayout: DataLayout,
    ShowProperty: ShowProperty,
    UserSelectInput: UserSelectInput
  },
  methods: {
    showDialog(){
      this.createFileVisible=true;
      this.$nextTick(()=>{
        this.$refs.ShowProperty.loadFormInfo();
      });
    },
    closeCreateDialog(){
        this.createFileVisible=false;
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
   getData(){
     let _self = this;
      if(!_self.$refs.ShowProperty.validFormValue()){
        return null;
      }
     let formData = _self.$refs.ShowProperty.getFormData();
      let jsonStr = formData.get("metaData");
      let m = JSON.parse(jsonStr);
      if(_self.parentId&&_self.parentId!=""){
        m.push(["parentId",_self.parentId]);
      }
      
      if(_self.folderId&&_self.folderId!=""){
        m.push(["folderId",_self.folderId]);
      }
      let formdataNew = new FormData();
      formdataNew.append("metaData", JSON.stringify(m));
      if (_self.file != "") {
        //console.log(_self.file);
        formdataNew.append("mainFile", _self.file.raw);
      }
      return formdataNew;
   },
    saveOrStartup(isStartup) {
      let _self = this;
      if(!_self.$refs.ShowProperty.validFormValue()){
        return;
      }
      
      _self.butt=true;
      _self.loading = true;
      let formData = _self.$refs.ShowProperty.getFormData();
      let jsonStr = formData.get("metaData");
      let m = JSON.parse(jsonStr);
      if(_self.parentId&&_self.parentId!=""){
        m.push(["parentId",_self.parentId]);
      }
      
      if(_self.folderId&&_self.folderId!=""){
        m.push(["folderId",_self.folderId]);
      }
      let formdataNew = new FormData();
      formdataNew.append("metaData", JSON.stringify(m));
      if (_self.file != "") {
        //console.log(_self.file);
        formdataNew.append("mainFile", _self.file.raw);
      }
      
      axios
        .post(url, formdataNew, {
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
                _self.createFileVisible=false;
                _self.$emit("saveOrstartSuccess");
            }
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
        _self.createFileVisible=false;
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
      this.fileAttachList = fileList;
      //console.log(file);
      // console.log(fileList);
    },
    
  }
};
</script>
<style scoped>
  
</style>
