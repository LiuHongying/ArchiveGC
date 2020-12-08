<template>
  <DataLayout>
    <template v-slot:header>
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
              <el-option :label="$t('application.all')+' '+$t('application.subDC')" value></el-option>
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
        <DataGrid
          ref="searchDoc"
          key="searchDoc"
          :dataUrl="param.searchViewUrl"
          v-bind:tableHeight="tableHeight"
          v-bind:isshowOption="true"
          v-bind:isshowSelection="true"
          :gridViewName="param.searchViewName"
          :condition="searchFileCondition+param.searchViewCondition"
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
            :isshowPage="isShowPage"
            @selectchange="selectInArchiveFile"
          ></DataGrid>
        </el-row>
        <div slot="footer" class="dialog-footer">
          <el-button @click="volumesFileVisible = false">{{$t('application.cancel')}}</el-button>
        </div>
      </el-dialog>
    </template>
    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
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
                          <el-button type="warning" @click="removeRelation">{{ $t("application.delete") }}</el-button>
                    </el-form-item>
                  </template>
                </el-form>
              </el-col>
            </el-row>
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
            >
            </DataGrid>
          </el-tab-pane>
        </el-tabs>
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
    MountFile:MountFile
  },
  model: {
    prop:"files",
    event: "change"
  },
  props: {
    files:{type:Array,default:[]},
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
      startHeight: 320,
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
      selectedInArchive: [],
      selectedRemoveFiles:[]
    };
  },
  mounted() {
    this.getTypeNamesByMainList("DCTypeSubContractor");
  },
  methods: {
      removeRelation(){
      let ids = []
      let _self = this
      ids.push(_self.parentId)
      this.selectedRemoveFiles.forEach(function(item){
                ids.push(item.ID)
            })
      axios.post("/exchange/doc/deleteRelations",ids).then(function(response){
        console.log(response)
        let code = response.data.code
        if(code==0){
          _self.$message("删除成功")
        }
                  _self.$refs.fileList.loadGridData()
      })
    },

    selectInArchiveFile(val) {
      this.selectedInArchive = val;
    },
    showVolumesFile(row) {
      let _self = this;
      console.log(row)
      _self.archiveId = row.ID;
      _self.volumesFileVisible = true;
      _self.$nextTick(() => {
        _self.$refs.fileInArchiveList.loadGridData();
      });
    },
    archiveSelect(val) {
      this.selectedRemoveFiles = val;
    },
    pendNot() {
      let _self = this;
      if (_self.selectedArchives.length == 0) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.PleaseSelectOneOrMoreData"),
          duration: 2000,
          type: "error"
        });
        return;
      }
      _self.pendNotVisible = true;
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
      let ids = []
      
      if (_self.$refs.fileList.itemDataList == null) {
        _self.$refs.fileList.itemDataList = _self.selectedFiles;
      } else {
        _self.selectedFiles.forEach(e => {
          let isContain = false;
          _self.$refs.fileList.itemDataList.find(function(value) {
            if (value === e) {
              isContain = true;
              return;
              //则包含该元素
            }
          });
          if (!isContain) {
            _self.$refs.fileList.itemDataList.push(e);
          }
        });
        
      }
      ids.push(_self.parentId)
      this.selectedFiles.forEach(function(item){
                ids.push(item.ID)
            })
      axios.post("/exchange/doc/AddRelations",ids).then(function(response){
        console.log(response)
        let code = response.data.code
        if(code==0){
          _self.$message("添加成功")
        }
      _self.$refs.fileList.loadGridData()
      })
      _self.$emit("change", _self.$refs.fileList.itemDataList);
      this.butt = false;
      this.propertyVisible = false;
    },
    searchItem() {
      let _self = this;
      let key = " 1=1 ";
      if (_self.searchFileCondition != "") {
        key += " and (" + searchFileCondition + ")";
      }
      if (_self.filters.docType != "") {
        key += " and TYPE_NAME = '" + _self.filters.docType + "'";
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
    },
    removeFile(){
      let _self=this;
      for(let i=0;i<_self.selectedRemoveFiles.length;i++){
        let e=_self.selectedRemoveFiles[i];
        for(let n=0;n<_self.$refs.fileList.itemDataList.length;n++){
          if(e.ID==_self.$refs.fileList.itemDataList[n].ID){
            _self.$refs.fileList.itemDataList.splice(n, 1);
            n--;
          }
        }
        _self.selectedRemoveFiles.splice(i,1);
        i--;
      }

      _self.selectedRemoveFiles.forEach(e=>{

        
        
      });
    },
  }
};
</script>
<style scoped>
</style>