<template>
  <div>
    <el-row>
        <ShowProperty
          v-if="true"
          ref="ShowPropertyForm"
          @onSaved="onSaved"
          width="100%"
          folderPath
          :showUploadFile="showUploadFile"
          v-bind:typeName="typeName"
          :itemId="formId"
        ></ShowProperty>

      </el-row>
      <el-row>
        <BorrowFileViewTask
          ref="workflowFile"
          :allowEdit="allowEdit"
          :isShowPage="true"
          v-model="workflowFileList"
          :activityName="activityName"
          :processDefinitionId="processDefinitionId"
          :parentId="formId"
          :isShowReject="isShowReject"
        ></BorrowFileViewTask>
      </el-row>
  </div>
</template>
<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGrid";
import AddCondition from "@/views/record/AddCondition";
import RejectButton from "@/components/RejectButton";
import ExcelUtil from "@/utils/excel.js";
import DataSelect from "@/components/ecm-data-select";
import DataLayout from "@/components/ecm-data-layout";
import AttachmentFile from "@/views/dc/AttachmentFile.vue";
import BorrowFileViewTask from "@/views/workflow/CancelFileViewTask.vue";
import ShowPropertyReadOnly from "@/components/ShowPropertyReadOnly.vue";
export default {
  name: "StartupWorkflow",
  permit: 1,
  components: {
    ShowProperty: ShowProperty,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    DataSelect: DataSelect,
    RejectButton: RejectButton,
    DataLayout: DataLayout,
    AttachmentFile: AttachmentFile,
    ShowPropertyReadOnly: ShowPropertyReadOnly,
    BorrowFileViewTask : BorrowFileViewTask
  },
  data() {
    return {
      // 本地存储高度名称
      topStorageName: "ReceivedDCHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 120,
      dialogName: "",
      propertyVisible: false,
      typeName: "",
      isOnly: false,
      butt: false,
      workflowFileList: [],
      saveButt: false
    };
  },
  props: {
    formId: {
      type: String,
      default: ""
    },
    allowEdit: { type: Boolean, default: true },
    showUploadFile: { type: Boolean, default: true },
    processDefinitionId: { type: String, default: "" },
    activityName: { type: String, default: "" },
    isShowReject:{type:Boolean,default:false}
  },
  mounted() {
    // this.getWorkflows();
  },

  methods: {
    
    loadFormInfo() {
      //   this.$refs.ShowPropertyForm.myItemId = this.formId;
      this.$refs.ShowPropertyForm.loadFormInfo();
    },
    sendData() {
      this.$emit("click", this.$refs.ShowPropertyForm.getFormData());
    },
    startUpWorkflow(workflow) {
      let _self = this;
      _self.butt = true;
      if (!this.$refs.ShowPropertyForm.validFormValue()) {
        _self.butt = false;
        return;
      }
      _self.butt = true;
      var m = new Map();
      let fileIds = new Array();
      for (let n = 0; n < _self.workflowFileList.length; n++) {
        fileIds.push(_self.workflowFileList[n].ID);
      }
      m.set("childFileId", fileIds);
      var c;
      for (c in _self.$refs.ShowPropertyForm.dataList) {
        let dataRows = _self.$refs.ShowPropertyForm.dataList[c].ecmFormItems;
        var i;
        for (i in dataRows) {
          if (dataRows[i].attrName && dataRows[i].attrName != "") {
            if (
              dataRows[i].attrName != "FOLDER_ID" &&
              dataRows[i].attrName != "ID"
            ) {
              var val = dataRows[i].defaultValue;
              if (val && dataRows[i].isRepeat) {
                var temp = "";
                // console.log(val);
                for (let j = 0, len = val.length; j < len; j++) {
                  temp = temp + val[j] + ";";
                  //console.log(temp);
                }
                temp = temp.substring(0, temp.length - 1);
                val = temp;
                console.log(val);
              }
              m.set(dataRows[i].attrName, val);
            }
          }
        }
      }
      if (_self.$refs.ShowPropertyForm.myItemId != "") {
        m.set("ID", _self.$refs.ShowPropertyForm.myItemId);
      }
      if (_self.$refs.ShowPropertyForm.myTypeName != "") {
        m.set("TYPE_NAME", _self.$refs.ShowPropertyForm.myTypeName);
        m.set("parentDocId", _self.parentId);
        m.set("relationName", _self.relationName);
        console.log(_self.$refs.ShowPropertyForm.myTypeName);
        console.log(_self.$refs.ShowPropertyForm.myFolderId);
      }
      _self.validateData(m, function(isOk) {
        _self.isOnly = isOk;

        if (_self.isOnly == false) {
          _self.$message({
            showClose: true,
            message: _self.$t("message.dataIsnotOnly"),
            duration: 2000,
            type: "error"
          });
          _self.butt = false;
          return;
        }
        let formdata = new FormData();
        formdata.append("metaData", JSON.stringify(m));
        if (_self.$refs.ShowPropertyForm.file != "") {
          //console.log(_self.file);
          formdata.append("uploadFile", _self.$refs.ShowPropertyForm.file.raw);
        }
        // console.log(JSON.stringify(m));
        if (_self.$refs.ShowPropertyForm.myItemId == "") {
          axios
            .post("/dc/createWorkflowFormData", formdata, {
              "Content-Type": "multipart/form-data"
            })
            .then(function(response) {
              let code = response.data.code;
              //console.log(JSON.stringify(response));
              if (code == 1) {
                _self.$message({
                  showClose: true,
                  message: _self.$t("message.newSuccess"), //_self.$t('message.newSuccess')
                  duration: 2000,
                  type: "success"
                });
                //发起流程

                _self.butt = false;
                _self.propertyVisible = false;
                _self.propertyrela = false;

                m.set("formId", response.data.id);

                m.set("processName", workflow.NAME);
                m.set("processInstanceId", workflow.ID);
                axios
                  .post("/workflow/startWorkflowById", JSON.stringify(m))
                  .then(function(response) {
                    console.log(response);
                    _self.$message({
                      showClose: true,
                      message: "流程发起成功!",
                      duration: 2000,
                      type: "success"
                    });
                    _self.closePage();
                  })
                  .catch(function(error) {
                    _self.$message({
                      showClose: true,
                      message: "流程发起失败!",
                      duration: 2000,
                      type: "warning"
                    });
                    console.log(error);
                  });
              } else if (response.data.MES != "") {
                _self.$message({
                  showClose: true,
                  message: response.data.MES,
                  duration: 2000,
                  type: "warning"
                });
                _self.butt = false;
              } else {
                _self.$message({
                  showClose: true,
                  message: _self.$t("message.newFailured"),
                  duration: 2000,
                  type: "warning"
                });
                _self.butt = false;
              }
            })
            .catch(function(error) {
              _self.$message(_self.$t("message.newFailured"));
              console.log(error);
              _self.butt = false;
            });
        }
      });
    },
    closePage(pv) {
      this.$emit("close");
    },
    // 保存文档
    saveItem() {
      let _self = this;
      _self.saveButt = true;
      if (!this.$refs.ShowPropertyForm.validFormValue()) {
        _self.saveButt = false;
        return;
      }
      var m = new Map();
      let fileIds = new Array();
      for (let n = 0; n < _self.workflowFileList.length; n++) {
        fileIds.push(_self.workflowFileList[n].ID);
      }
      m.set("childFileId", fileIds);
      var c;
      for (c in _self.$refs.ShowPropertyForm.dataList) {
        let dataRows = _self.$refs.ShowPropertyForm.dataList[c].ecmFormItems;
        var i;
        for (i in dataRows) {
          if (dataRows[i].attrName && dataRows[i].attrName != "") {
            if (
              dataRows[i].attrName != "FOLDER_ID" &&
              dataRows[i].attrName != "ID"
            ) {
              var val = dataRows[i].defaultValue;
              if (val && dataRows[i].isRepeat) {
                var temp = "";
                // console.log(val);
                for (let j = 0, len = val.length; j < len; j++) {
                  temp = temp + val[j] + ";";
                  //console.log(temp);
                }
                temp = temp.substring(0, temp.length - 1);
                val = temp;
                console.log(val);
              }
              m.set(dataRows[i].attrName, val);
            }
          }
        }
      }
      if (_self.$refs.ShowPropertyForm.myItemId != "") {
        m.set("ID", _self.$refs.ShowPropertyForm.myItemId);
      }
      if (_self.$refs.ShowPropertyForm.myTypeName != "") {
        m.set("TYPE_NAME", _self.$refs.ShowPropertyForm.myTypeName);
        m.set("parentDocId", _self.parentId);
        m.set("relationName", _self.relationName);
        console.log(_self.$refs.ShowPropertyForm.myTypeName);
        console.log(_self.$refs.ShowPropertyForm.myFolderId);
      }
      _self.validateData(m, function(isOk) {
        _self.isOnly = isOk;

        if (_self.isOnly == false) {
          _self.$message({
            showClose: true,
            message: _self.$t("message.dataIsnotOnly"),
            duration: 2000,
            type: "error"
          });
          _self.saveButt = false;
          return;
        }
        let formdata = new FormData();
        formdata.append("metaData", JSON.stringify(m));
        if (_self.$refs.ShowPropertyForm.file != "") {
          //console.log(_self.file);
          formdata.append("uploadFile", _self.$refs.ShowPropertyForm.file.raw);
        }
        // console.log(JSON.stringify(m));
        if (_self.$refs.ShowPropertyForm.myItemId == "") {
          axios
            .post("/dc/createWorkflowFormData", formdata, {
              "Content-Type": "multipart/form-data"
            })
            .then(function(response) {
              let code = response.data.code;
              //console.log(JSON.stringify(response));
              if (code == 1) {
                _self.$message({
                  showClose: true,
                  message: _self.$t("message.newSuccess"), //_self.$t('message.newSuccess')
                  duration: 2000,
                  type: "success"
                });
                //发起流程

                _self.saveButt = false;
                _self.propertyVisible = false;
                _self.propertyrela = false;
              } else if (response.data.MES != "") {
                _self.$message({
                  showClose: true,
                  message: response.data.MES,
                  duration: 2000,
                  type: "warning"
                });
                _self.butt = false;
              } else {
                _self.$message({
                  showClose: true,
                  message: _self.$t("message.newFailured"),
                  duration: 2000,
                  type: "warning"
                });
                _self.butt = false;
              }
            })
            .catch(function(error) {
              _self.$message(_self.$t("message.newFailured"));
              console.log(error);
              _self.butt = false;
            });
        }
      });
    },

    // 保存结果事件
    onSaved(indata) {
      let _self = this;
      if (indata == "update") {
        // _self.$message(_self.$t("message.saveSuccess"));
        _self.$message({
          showClose: true,
          message: _self.$t("message.saveSuccess"),
          duration: 2000,
          type: "success"
        });
        _self.butt = false;
      } else {
        // _self.$message("新建成功!");
        _self.$message({
          showClose: true,
          message: _self.$t("message.operationSuccess"),
          duration: 2000,
          type: "success"
        });
        _self.butt = false;
      }
      _self.propertyVisible = false;
      _self.propertyrela = false;
      this.butt = false;
    }
  }
};
</script>
<style scoped>
</style>