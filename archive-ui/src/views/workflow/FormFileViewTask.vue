<template>
  <DataLayout>
    <template v-slot:header>
      <el-dialog :visible.sync="typeSelectVisible" :append-to-body="true">
      <el-form>
        <el-form-item :label="$t('application.fileType')" :rules="[{required:true,message:'必填',trigger:'blur'}]">
          <el-select
            name="selectName"
            v-model="selectedClassic"
            :placeholder="$t('application.selectFileType')"
            style="display:block;"
            @change="getTypeNameByClassic(selectedClassic)"
          >
            <div v-for="(name,nameIndex) in classicNames" :key="'T_'+nameIndex">
              <el-option :label="name" :value="name" :key="nameIndex"></el-option>
            </div>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('application.fileType')" :rules="[{required:true,message:'必填',trigger:'blur'}]">
          <el-select
            name="selectName"
            v-model="selectedTypeName"
            :placeholder="$t('application.selectFileType')"
            style="display:block;"
          >
            <div v-for="(name,nameIndex) in typeNames" :key="'T_'+nameIndex">
              <el-option :label="name" :value="name" :key="nameIndex"></el-option>
            </div>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button
          @click="typeSelectVisible=false;newArchiveItem(selectedTypeName,parentId)"
        >{{$t('application.ok')}}</el-button>
      </div>
    </el-dialog>
	
	<el-dialog
      :title="dialogName+$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
      width="80%"
      :append-to-body="true"
    >
      <ShowProperty
        ref="ShowProperty"
        @onSaved="onSaved"
        width="100%"
        :folderPath="folderPath"
        v-bind:itemId="selectedItemId"
        v-bind:typeName="typeName"
      ></ShowProperty>
      <div slot="footer" class="dialog-footer">
        <el-button @click="saveItem">{{$t('application.save')}}</el-button>
        <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
      </div>
    </el-dialog>
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
        <el-row v-if="allowEdit" span="24">
           <el-form :inline="true" :model="filters" @submit.native.prevent>
              <el-form-item>
              <el-button type="primary" @click="beforeCreateFile(archiveRow)">{{ $t("application.new") }}</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="danger" @click="onDeleleItem(selectedInArchive,[$refs.fileInArchiveList])">{{ $t("application.delete") }}</el-button>
              </el-form-item>
              <el-form-item>
              <MountFile
                :selectedItem="selectedInArchive"
                :title="$t('application.ReplaceDoc')"
              >{{$t('application.replace')}}</MountFile>
              </el-form-item>
              </el-form>
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
                  <template v-if="isShowReject">
                    <el-form-item>
                      <el-button type="primary" @click="pendNot">{{ $t("application.pendNot") }}</el-button>
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
            >
              <template slot="sequee" slot-scope="scope">
                <span
                  :style="(scope.data.row['C_REJECT_COMMENT']!=null
                      &&scope.data.row['C_REJECT_COMMENT']!='')?{'background':'red'}:''"
                >{{(scope.currentPage-1) * scope.pageSize+ scope.data.$index+1}}</span>
              </template>
            </DataGrid>
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
    MountFile:MountFile
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
      selectedInArchive: [],
      typeSelectVisible:false,
      selectedItemId:"",
      folderPath:"",
      classicNames:[],
      typeNames: [],
      selectedTypeName: [],
      pid:"",
      archiveRow:[]
    };
  },
  mounted() {
    this.getTypeNamesByMainList("DCTypeSubContractor");
    this.getClassicNames("ClassicNames");
  },
  methods: {
    getTypeNameByClassic(keyName){
      let _self = this;
      _self.typeNames=[];
      axios
        .post("/dc/getEcmDefTypes", keyName)
        .then(function(response) {
          if(response.data.code==1){
            response.data.data.forEach(element => {
              _self.typeNames.push(element.name);
            });
          }
          
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    getTypeNames(keyName) {
      let _self = this;
      axios
        .post("/dc/getParameters", keyName)
        .then(function(response) {
          _self.typeNames = response.data.data.innerTransferDocType;
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    getClassicNames(keyName) {
      let _self = this;
      let pm = new Map();
      pm.set("configName", keyName);
      // pm.set('parentId',"'"+p+"'");
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
          method: "post",
          data: JSON.stringify(pm),
          url: "/dc/getObjectsByConfigClauseNoPage",
        })
        .then(function (response) {
          var i;
          let temp = response.data.data;
          for (i = 0; i < temp.length; i++) {
            _self.classicNames.push(temp[i].NAME)
          }
          console.log(_self.contractors);
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    selectInArchiveFile(val) {
      this.selectedInArchive = val;
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
            _self.$refs.fileList.loadGridData();
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
      _self.archiveRow=row;
      _self.archiveId = row.ID;
      _self.pid=_self.archiveId;
      _self.volumesFileVisible = true;
      _self.$nextTick(() => {
        _self.$refs.fileInArchiveList.loadGridData();
      });
    },
    archiveSelect(val) {
      this.selectedArchives = val;
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

    beforeNewDocument(){
      let _self=this;
      if(_self.parentId==undefined){
        // _self.$message(_self.$t("message.pleaseSelectOneTransfer"));
        
        _self.$message({
                showClose: true,
                message: _self.$t("message.pleaseSelectOneTransfer"),
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self.typeSelectVisible=true;
    },
    //著录文件
    beforeCreateFile(row){
      let _self=this;
      
      if(row==undefined){
        // _self.$message('请选择一条图册或卷盒数据！')
        
         _self.$message({
                showClose: true,
                message: '请选择一条案卷数据！',
                duration: 2000,
                type: "warning"
              });
        return;
      }
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: row.TYPE_NAME,
          url: "/dc/getArchiveFileConfig"
        })
        .then(function(response) {
          let code=response.data.code;
          if(code=='1'){
            let data=response.data.data;
            let fileType=data[0].C_TO;
            _self.newArchiveItem(fileType,row.ID);
          }else{
            _self.$message({
                showClose: true,
                message: response.data.msg,
                duration: 5000,
                type: "error"
              });
          }
        })
        .catch(function(error) {
          // _self.$message("添加失败！");
          _self.$message({
                showClose: true,
                message: '添加失败！',
                duration: 5000,
                type: "error"
              });
          console.log(error);
        });
    },	
	  newArchiveItem(typeName, paId) {
      let _self = this;
      if (paId) {
        _self.selectedItemId = "";

        _self.dialogName = typeName;
        _self.typeName=typeName;
        
        _self.folderPath = '/移交库';
        // _self.deliverDocId=selectedRow.ID
        _self.selectedItemId="";
        _self.pid=paId;
        _self.propertyVisible = true;
        _self.$nextTick(()=>{
          _self.$refs.ShowProperty.loadFormInfo();
        });
        

      } else {
        // _self.$message(_self.$t("message.pleaseSelectOneTransfer"));
        _self.$message({
                showClose: true,
                message: _self.$t("message.pleaseSelectOneTransfer"),
                duration: 2000,
                type: "warning"
              });
      }
    },
	
	// 保存文档
    saveItem() {
      if (!this.$refs.ShowProperty.validFormValue()) {
        return;
      }
      let _self = this;
      var m = new Map();
      let dataRows = this.$refs.ShowProperty.dataList;
      var c;
        for(c in _self.$refs.ShowProperty.dataList)
        {
            let dataRows = _self.$refs.ShowProperty.dataList[c].ecmFormItems;
            var i;
            for (i in dataRows) {
            if(dataRows[i].attrName && dataRows[i].attrName !='')
            {
                if(dataRows[i].attrName !='FOLDER_ID'&&dataRows[i].attrName !='ID')
                {
                var val = dataRows[i].defaultValue;
                if(val && dataRows[i].isRepeat){
                    var temp = "";
                // console.log(val);
                    for(let j=0,len=val.length;j<len;j++){
                    temp = temp + val[j]+";";
                    //console.log(temp);
                    }
                    temp = temp.substring(0,temp.length-1);
                    val = temp;
                    console.log(val);
                }
                m.set(dataRows[i].attrName, val);
                }
            }
            }
        }
      if (_self.$refs.ShowProperty.myItemId != "") {
        m.set("ID", _self.$refs.ShowProperty.myItemId);
      }
      if (_self.$refs.ShowProperty.myTypeName != "") {
        m.set("TYPE_NAME", _self.typeName);
        m.set("folderPath", _self.folderPath);
        m.set("transferId", _self.pid);
      }
      let formdata = new FormData();
      formdata.append("metaData", JSON.stringify(m));

      if (_self.$refs.ShowProperty.file != "") {
        //console.log(_self.file);
        formdata.append("uploadFile", _self.$refs.ShowProperty.file.raw);
      }
      // console.log(JSON.stringify(m));
      if (_self.$refs.ShowProperty.myItemId == "") {
        _self
          .axios({
            headers: {
              "Content-Type": "multipart/form-data"
              // x-www-form-urlencoded'
              //"Content-Type": "application/json;charset=UTF-8"
            },
            method: "post",
            data: formdata,
            url: "/dc/newArchiveOrDocument"
          })
          .then(function(response) {
            let code = response.data.code;
            //console.log(JSON.stringify(response));
            if (code == 1) {
              // _self.$message(_self.$t('message.newSuccess'));
              _self.$message({
                showClose: true,
                message: _self.$t('message.newSuccess'),
                duration: 2000,
                type: "success"
              });
              _self.propertyVisible = false;
              if(!_self.volumesFileVisible){
                _self.$refs.fileList.loadGridData();
              }else{
                _self.$refs.fileInArchiveList.loadGridData();
              }
              
              
            } else {
              // _self.$message(_self.$t('message.newFailured'));
              _self.$message({
                showClose: true,
                message: _self.$t('message.newFailured'),
                duration: 2000,
                type: "warning"
              });
            }
          })
          .catch(function(error) {
            // _self.$message(_self.$t('message.newFailured'));
            _self.$message({
                showClose: true,
                message: _self.$t('message.newFailured'),
                duration: 5000,
                type: "error"
              });
            console.log(error);
          });
      } 
    },
    
    beforeAddFile() {
      let _self = this;
      
    },
    fileSelect(val) {
      this.selectedFiles = val;
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
    }
  }
};
</script>
<style scoped>
</style>