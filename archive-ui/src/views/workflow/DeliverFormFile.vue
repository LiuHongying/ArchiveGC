<template>
  <DataLayout>
    <template v-slot:header>
      <el-dialog
        :title="$t('application.pendNot')"
        :visible.sync="pendNotVisible"
        @close="pendNotVisible = false"
        :append-to-body="true"
        width="60%"
        :close-on-click-modal="false"
        v-dialogDrag
      >
        <el-col :span="24">
          <el-form label-position="right" :model="pendForm" @submit.native.prevent>
            <el-input type="textarea" :rows="6" v-model="pendForm.rejectComment" autocomplete="off"></el-input>
          </el-form>
        </el-col>
        <div slot="footer" class="dialog-footer">
          <el-button @click="saveRejectComment" :loading="butt">{{$t('application.save')}}</el-button>
          <el-button @click="pendNotVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
      <el-dialog
        :title="$t('application.AddFile')"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        :append-to-body="true"
        width="90%"
        :close-on-click-modal="false"
        v-dialogDrag
      >
        <el-form :inline="true" :model="filters" @submit.native.prevent>
          <el-form-item>
            <el-select v-model="filters.docType">
              <el-option :label="$t('application.all')" value></el-option>
              <el-option
                v-for="(name,nameIndex) in childrenTypes"
                :key="'Type2_'+nameIndex"
                :label="name"
                :value="name"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="filters.title"
              :placeholder="$t('application.Coding')+$t('application.or')+$t('application.Title')"
              @keyup.enter.native="searchItem"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" v-on:click="searchItem">{{$t('application.SearchData')}}</el-button>
          </el-form-item>
        </el-form>
        <!--searchView
        dataUrl="/dc/getDocuments"
        gridViewName="WorkflowFileGrid"
        :condition="searchFileCondition"
        -->
        <DataGrid
          ref="searchDoc"
          key="searchDoc"
          :dataUrl="param.searchViewUrl"
          v-bind:tableHeight="tableHeight"
          v-bind:isshowOption="true"
          v-bind:isshowSelection="true"
          :gridViewName="param.searchViewName"
          :condition="searchFileCondition+' and '+param.searchViewCondition"
          :optionWidth="1"
          :isShowMoreOption="false"
          :isshowCustom="false"
          :isEditProperty="false"
          :isShowChangeList="false"
          :isshowicon="false"
          @selectchange="fileSelect"
        ></DataGrid>
        <div slot="footer" class="dialog-footer">
          <el-button @click="saveFileToWorkflow" :loading="butt">{{$t('application.save')}}</el-button>
          <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
      <el-dialog
        :title="$t('application.VolumesFile')"
        :visible.sync="volumesFileVisible"
        @close="volumesFileVisible = false"
        :append-to-body="true"
        width="90%"
        :close-on-click-modal="false"
        v-dialogDrag
      >
        <!--卷内文件列表
        gridViewName="WorkflowFileGrid"
        condition=" and a.NAME='irel_children'"
        dataUrl="/dc/getDocuByRelationParentId"
        -->
        <el-row v-if="allowEdit">
          <MountFile
            :selectedItem="selectedInArchive"
            :title="$t('application.ReplaceDoc')"
          >{{$t('application.replace')}}</MountFile>
        </el-row>
        <el-row>
          <DataGrid
            ref="fileInArchiveList"
            key="fileInArchiveList"
            :parentId="archiveId"
            v-bind:tableHeight="tableHeight"
            v-bind:isshowOption="true"
            v-bind:isshowSelection="true"
            :dataUrl="param.childViewUrl"
            :gridViewName="param.childViewName"
            :condition="param.childviewCondition"
            :optionWidth="1"
            :isShowPropertyButton="false"
            :isShowMoreOption="true"
            :isshowCustom="false"
            :isEditProperty="allowEdit"
            :isShowChangeList="false"
            :isshowicon="false"
            showOptions="查看属性,查看内容"
            @selectchange="selectInArchiveFile"
            :isshowPage="isShowPage"
          ></DataGrid>
        </el-row>
        <div slot="footer" class="dialog-footer">
          <el-button @click="volumesFileVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
    </template>
    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <!-- <div :style="{position:'relative',height: layout.height-startHeight+'px'}"> -->
        <!-- <split-pane
          v-on:resize="onSplitResize"
          :min-percent="20"
          :default-percent="topPercent"
          split="horizontal"
        >
        <template slot="paneL">-->
        <el-tabs value="t01">
          <el-tab-pane :label="$t('application.FilesInWorkflow')" name="t01">
            <el-row v-if="allowEdit||isShowReject">
              <el-col :span="24" style="text-align: left">
                <el-form :inline="true" :model="filters" @submit.native.prevent>
                  <template v-if="allowEdit">
                    <el-form-item>
                      <el-button type="primary" @click="beforeAddFile">{{ $t("application.new") }}</el-button>
                    </el-form-item>
                    <el-form-item>
                      <el-button
                        type="warning"
                        @click="deleteRelation"
                      >{{ $t("application.delete") }}</el-button>
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
            <!--列表 dataUrl="/dc/getDocuByRelationParentId"
                gridViewName="WorkflowFileGrid"
                condition=" and a.NAME='irel_children'"
            -->
            <DataGrid
              ref="fileList"
              key="fileList"
              :parentId="parentId"
              :dataUrl="param.archiveViewUrl"
              v-bind:tableHeight="tableHeight"
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              :gridViewName="param.archiveViewName"
              :condition="param.archiveViewCondition"
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
            ></DataGrid>
          </el-tab-pane>
        </el-tabs>
        <!-- </template>
        </split-pane>-->
      </div>
    </template>
  </DataLayout>
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
import MountFile from "@/components/MountFile.vue";
export default {
  components: {
    ShowProperty: ShowProperty,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    DataSelect: DataSelect,
    RejectButton: RejectButton,
    DataLayout: DataLayout,
    AttachmentFile: AttachmentFile,
    MountFile: MountFile
  },
  model: {
    event: "change"
  },
  props: {
    allowEdit: { type: Boolean, default: true },
    isShowPage: { type: Boolean, default: true },
    parentId: { type: String, default: "" },
    processDefinitionId: { type: String, default: "" },
    activityName: { type: String, default: "" },
    isShowReject: { type: Boolean, default: false },
    param: { type: Object, default: null }
  },
  data() {
    return {
      // 本地存储高度名称
      topStorageName: "ReceivedDCHeight",
      // 非split pan 控制区域高度
      startHeight: 255,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 120,
      childrenTypes: [],
      propertyVisible: false,
      filters: {
        projectCode: "",
        docType: "",
        coding: "",
        title: "",
        limit: 10,
        typeName: "",
        relationName: ""
      },
      pendForm: {
        rejectComment: ""
      },
      selectedFiles: [],
      butt: false,
      searchFileCondition: "",
      selectedArchives: [],
      archiveId: "", //案卷ID
      volumesFileVisible: false,
      pendNotVisible: false,
      childIds: [],
      selectedInArchive:[]
    };
  },
  mounted() {
    this.getTypeNamesByMainList("BusinessClassic");
  },
  methods: {
    selectInArchiveFile(val){
      this.selectedInArchive=val;
    },
    saveRejectComment() {
      let _self = this;
      if (_self.pendForm.rejectComment == "") {
        _self.$message({
          showClose: true,
          message: _self.$t("message.PleaseInputData"),
          duration: 2000,
          type: "error"
        });
        return;
      }
      let ids = new Array();
      _self.selectedArchives.forEach(function(e) {
        ids.push(e.ID);
      });

      let m = new Map();
      m.set("ids", ids);
      m.set("comment", _self.pendForm.rejectComment);
      axios
        .post("/dc/savePenNot", JSON.stringify(m)) ///dc/getSelectList
        .then(function(response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "success"
            });
            _self.pendNotVisible = false;
          } else {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveFailured"),
              duration: 2000,
              type: "error"
            });
          }
        })
        .catch(function(error) {
          console.log(error);
          _self.$message({
            showClose: true,
            message: _self.$t("message.saveFailured"),
            duration: 2000,
            type: "error"
          });
        });
    },
    showVolumesFile(row) {
      let _self = this;
      _self.archiveId = row.ID;
      _self.volumesFileVisible = true;
      _self.$nextTick(() => {
        _self.$refs.fileInArchiveList.loadGridData();
      });
    },
    archiveSelect(val) {
      this.selectedArchives = val;
    },
    deleteRelation() {
      let _self = this;
      if (this.selectedArchives && this.selectedArchives.length > 0) {
        let ids = new Array();
        this.selectedArchives.forEach(e => {
          ids.push(e["RELATION_ID"]);
        });
        this.delRelation(ids, function() {
          _self.$refs.fileList.loadGridData();
        });
      } else {
        _self.$message({
          showClose: true,
          message: _self.$t("message.PleaseSelectOneOrMoreData"),
          duration: 2000,
          type: "error"
        });
      }
    },
    beforeAddFile() {
      let _self = this;
      this.getEcmcfgActive(
        this.processDefinitionId,
        this.activityName,
        function(ecmCfgActivity) {
          _self.searchFileCondition = ecmCfgActivity.formCondition;
          _self.propertyVisible = true;
        }
      );
    },
    fileSelect(val) {
      this.selectedFiles = val;
    },
    saveFileToWorkflow() {
      let _self = this;
      if (_self.$refs.fileList.itemDataList != null) {
        _self.selectedFiles.forEach(e => {
          _self.childIds.push(e.ID);
        });
      } else {
        _self.selectedFiles.forEach(e => {
          let isContain = false;
          _self.$refs.fileList.itemDataList.find(function(value) {
            if (value.ID === e.ID) {
              isContain = true;
              return;
              //则包含该元素
            }
          });
          if (!isContain) {
            _self.childIds.push(e.ID);
          }
        });
        // this.$refs.fileList.itemDataList=this.$refs.fileList.itemDataList.concat(this.selectedFiles);
      }
      let m = new Map();
      m.set("childIds", _self.childIds);
      m.set("relationName", "irel_parent");
      m.set("parentId", _self.parentId);
      axios
        .post("/dc/addRelation", JSON.stringify(m))
        .then(function(response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "success"
            });
            _self.butt = false;
            _self.propertyVisible = false;
            _self.$refs.fileList.loadGridData();
          } else {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveFailured"),
              duration: 2000,
              type: "error"
            });
          }
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.$message({
            showClose: true,
            message: _self.$t("message.saveFailured"),
            duration: 2000,
            type: "error"
          });
          _self.loading = false;
        });
    },
    searchItem() {
      let _self = this;
      let key = " 1=1 ";
      if (_self.searchFileCondition != "") {
        key += " and (" + _self.searchFileCondition + ")";
      }

      if (_self.filters.docType != "") {
        key +=
          " and TYPE_NAME = '" +
          _self.filters.docType +
          "' and STATUS='已入库' and IS_HIDDEN =0 ";
      } else {
        if (_self.param.searchViewCondition != "") {
          key += " and (" + _self.param.searchViewCondition + ")";
        }
      }
      if (_self.filters.title != "") {
        key +=
          " and ( TITLE like '%" +
          _self.filters.title +
          "%' or CODING like '%" +
          _self.filters.title +
          "%' " +
          ")";
      }
      if (key != "") {
        _self.$refs.searchDoc.condition = key;
      }
      _self.$refs.searchDoc.loadGridData();
    },
    //获取类型
    getTypeNamesByMainList(keyName) {
      let _self = this;
      axios
        .post("/dc/getOneParameterValue", keyName)
        .then(function(response) {
          _self.childrenTypes = response.data.data;
        })
        .catch(function(error) {
          console.log(error);
        });
    }
  }
};
</script>
<style scoped>
</style>