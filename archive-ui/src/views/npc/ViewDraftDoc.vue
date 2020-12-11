<template>
  <DataLayout>
    <template v-slot:header>
      <!-- 创建设计文件附件 -->
      <el-dialog
        :title="$t('application.Import')"
        :visible.sync="importSubVisible"
        width="70%"
        :close-on-click-modal="false"
        :append-to-body="true"
      >
        <el-form
          size="mini"
          :label-width="formLabelWidth"
          v-loading="uploading"
        >
          <div style="height:200px;overflow-y:scroll; overflow-x:scroll;">
            <el-upload
              :limit="100"
              :file-list="fileAttachList"
              action
              :on-change="handleChangeAttach"
              :auto-upload="false"
              :multiple="true"
            >
              <el-button slot="trigger" size="small" type="primary">{{
                $t("application.selectFile")
              }}</el-button>
            </el-upload>
          </div>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="importSubVisible = false">{{
            $t("application.cancel")
          }}</el-button>
          <el-button type="primary" @click="uploadDataSub()">{{
            $t("application.start") + $t("application.Import")
          }}</el-button>
        </div>
      </el-dialog>
    </template>
    <template v-slot:main="{ layout }">
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
        <el-row>
          <el-collapse value="文件列表">
            <el-collapse-item
              title="文件列表"
              name="文件列表"
              id="uploadFile"
              key="cindex"
            >
              <el-row>
                <el-col :span="12">
                  <div class="el-button-ecm">
                    <el-button
                      type="success"
                      @click="showItemContent(selectedItemId)"
                      >查看主件</el-button
                    >
                  </div>
                  <div class="el-button-ecm">
                    <UploadOneFile :docId="selectedItemId" url="/dc/addRendition">格式副本</UploadOneFile>
                  </div>
                  <div class="el-button-ecm">
                    <MountFile :selectedItem="[{ ID: selectedItemId }]">{{
                      $t("application.ReplaceDoc")
                    }}</MountFile>
                  </div>

                  <div class="el-button-ecm">
                    <el-button
                      type="primary"
                      @click="beforeUploadSubFile('/dc/addAttachment')"
                    >
                      {{ $t("application.Add") + $t("application.Attachment") }}
                    </el-button>
                  </div>
                  <div class="el-button-ecm">
                    <el-button
                      type="danger"
                      @click="
                        onDeleleItem(selectedAttachment, [$refs.attachmentDoc])
                      "
                      >{{ $t("application.delete") }}</el-button
                    >
                  </div>
                </el-col>
              </el-row>
              <el-row>
                <!--列表-->
                <DataGrid
                  ref="attachmentDoc"
                  key="attachmentDocKey"
                  dataUrl="/dc/getDocuByRelationParentId"
                  v-bind:tableHeight="
                    ((layout.height - startHeight) * (100 - topPercent)) / 100 -
                      bottomHeight
                  "
                  v-bind:isshowOption="true"
                  v-bind:isshowSelection="true"
                  gridViewName="AttachmentGrid"
                  condition=" and a.NAME='附件'"
                  :optionWidth="2"
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
        </el-row>
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
                      v-bind:inputValue="vdata[approver.performerPolicy]"
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
      <el-row>
        <!-- <el-col :span="24">
                <el-button type="primary" @click="getData()" style="position:absolute;right:120px;margin:10px;">发起流程</el-button>
                <el-button style="position:absolute;right:50px;margin:10px;" >暂  存</el-button>
            </el-col> -->
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
import UploadOneFile from "@/components/UploadOneFile.vue";
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
      formLabelWidth: "120px",
      importSubVisible: false,
      uploadUrl: "",
      selectedAttachment: [],
      vdata:{}
    };
  },
  props: {
    selectedItemId: { type: String, default: "" }
  },
  mounted() {
    
    
  },
  components: {
    DataLayout: DataLayout,
    ShowProperty: ShowProperty,
    UserSelectInput: UserSelectInput,
    DataGrid: DataGrid,
    MountFile: MountFile,
    UploadOneFile:UploadOneFile
  },
  methods: {
    loadPerson(){
      let _self=this;
      _self.getObjectById(this.selectedItemId,function(data){
        _self.vdata=data;
      });
      _self.getApprovalUserList();
    },
    attachmentDocSelect(val) {
      this.selectedAttachment = val;
    },
    beforeUploadSubFile(uploadpath) {
      let _self = this;
      if (_self.selectedItemId == undefined || _self.selectedItemId == "") {
        // _self.$message('请选择一条文件数据');
        _self.$message({
          showClose: true,
          message: _self.$t("message.pleaseSelectOneDesigndoc"),
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self.uploadUrl = uploadpath;
      _self.fileAttachList = [];
      _self.importSubVisible = true;
    },
    getSubFormData() {
      let _self = this;
      let formdata = new FormData();
      var data = {};
      data["parentDocId"] = _self.selectedItemId; //_self.selectedInnerItems[0].ID;//_self.selectedFileId;
      data["relationName"] = "附件";
      data["TYPE_NAME"] = "附件";
      formdata.append("metaData", JSON.stringify(data));
      _self.fileAttachList.forEach(function(file) {
        //console.log(file.name);
        formdata.append("uploadFile", file.raw, file.name);
      });
      return formdata;
    },
    //上传文件
    uploadDataSub() {
      let _self = this;
      let formdata = _self.getSubFormData();
      console.log("UploadData getData");
      console.log(formdata);
      _self.uploading = true;
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          datatype: "json",
          method: "post",
          data: formdata,
          url: _self.uploadUrl
        })
        .then(function(response) {
          _self.importSubVisible = false;
          // _self.refreshData();
          _self.uploading = false;
          // _self.$message(_self.$t('application.Import')+_self.$t('message.success'));
          _self.$message({
            showClose: true,
            message:
              _self.$t("application.Import") + _self.$t("message.success"),
            duration: 2000,
            type: "success"
          });
          _self.$refs.attachmentDoc.loadGridData();
        })
        .catch(function(error) {
          _self.uploading = false;
          console.log(error);
        });
    },
    // 查看内容
    showItemContent(id) {
      if (!id) {
        return;
      }
      let condition = id;
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

    saveData(isStartupWorkflow) {
      let _self = this;
      let formData = _self.$refs.ShowProperty.getFormData();
      let jsonStr = formData.get("metaData");
      let m = JSON.parse(jsonStr);
      for (let key in _self.taskForm) {
        let p = new Array();
        p.push(key);
        p.push(_self.taskForm[key]);
        m.push(p);
      }
      // m.push(['ID',_self.selectedItemId]);
      axios
        .post("/dc/saveDocument", JSON.stringify(m))
        .then(function(response) {
          let code = response.data.code;
          //console.log(JSON.stringify(response));
          if (code == 1) {
            _self.$emit("onSaved", "update");
            _self.$emit("onSaveSuccess", m);
            if (isStartupWorkflow) {
              _self.$emit("onStartUpworkflow", m);
            }
          } else {
            _self.$message(_self.$t("message.saveFailured"));
          }
        })
        .catch(function(error) {
          _self.$message(_self.$t("message.saveFailured"));
          console.log(error);
        });
    },

    handleChangeAttach(file, fileList) {
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
