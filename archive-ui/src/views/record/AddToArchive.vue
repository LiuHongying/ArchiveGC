<template>
  <span>
    <el-dialog
      title="添加至案卷"
      :visible.sync="createFileVisible"
      @close="createFileVisible = false"
      width="90%"
      style="width:100%"
      :close-on-click-modal="false"
      @open="searchItem"
      v-dialogDrag
    >
      <DataLayout>
        <template v-slot:header></template>
        <template v-slot:main="{layout}">
          <el-row>
            <el-col :span="3" class="topbar-input">
              <el-input
                v-model="inputkey"
                :placeholder="$t('message.pleaseInput')+$t('application.keyword')"
                @change="searchItem"
                prefix-icon="el-icon-search"
              ></el-input>
            </el-col>
          </el-row>
          <el-row>
            <DataGrid
              ref="mainDataGrid"
              key="main"
              data-url="/dc/getInnerFolderDocuments"
              v-bind:tableHeight="(layout.height-startHeight-75)"
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              gridViewName="ArrangeGrid"
              :optionWidth="2"
              :condition="mainParam.condition"
              :folderId="mainParam.folderId"
              :isshowCustom="false"
              :isEditProperty="false"
              :isShowMoreOption="false"
              :isShowChangeList="false"
              :isInitData="false"
              :extparam="param"
              @rowclick="rowClick"
              @selectchange="selectChange"
            ></DataGrid>
          </el-row>
        </template>
      </DataLayout>
      <div slot="footer" class="dialog-footer">
        <slot name="save">
          <el-button type="success" :loading="butt" @click="saveItem();">确定</el-button>
        </slot>
        <slot name="close">
          <el-button @click="closeCreateDialog" size="medium">{{$t('application.close')}}</el-button>
        </slot>
      </div>
    </el-dialog>
    <el-button icon="el-icon-plus" @click="show" type="primary" plain size="small">
      添加至案卷
    </el-button>
  </span>
</template>
<script>
import DataGrid from "@/components/DataGrid";
import DataLayout from '@/components/ecm-data-layout'
export default {
  name: "addToArchive",
  components: {
      DataGrid:DataGrid,
      DataLayout:DataLayout
  },
  data() {
    return {
      condition: " C_ITEM_TYPE='案卷' and IS_HIDDEN=0 and  status='整编'",
      mainParam: {
        folderId: "C_ITEM_TYPE",
        condition: "  C_ITEM_TYPE='案卷' and IS_HIDDEN=0 and  status='整编'"
      },
      inputkey:"",
      currentId: "",
      createFileVisible: false,
      butt: false,
      fileIds:[],
    };
  },
  props: {
    folderId: { type: String, default: "" },
    archiveObjects: { type: Array, default: () => [] }
  },
  mounted() {
    this.mainParam.folderId = this.folderId;
  },
  methods: {
    saveItem() {
      let _self = this;
      _self.butt = true;
      let m = new Map();
    //   m.set("fileId", JSON.stringify(_self.fileIds));
    //   m.set("archiveId", _self.currentId);
      let formdata = new FormData();
      formdata.append("fileId", JSON.stringify(_self.fileIds));
      formdata.append("archiveId", _self.currentId);
      axios({
        headers: {
          "Content-Type": "application/json;charset=UTF-8"
        },
        method: "post",
        data: formdata,
        url: "/dc/addToStorageArchive"
      })
        .then(function(response) {
            _self.butt = false;
          if ("1" == response.data.code) {
            _self.$message({
              showClose: true,
              message: "保存成功！",
              duration: 2000,
              type: "success"
            });
            _self.createFileVisible=false;
            _self.$emit("savesuccess");
          } else {
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "error"
            });
          }
        })
        .catch(function(error) {
            _self.butt = false;
            _self.$message({
              showClose: true,
              message: "保存失败"+error.message,
              duration: 2000,
              type: "error"
            });
          console.log(error);
        });
    },
    show() {
      this.archiveObjects.forEach((e)=>{
        this.fileIds.push(e.ID);
      });
      this.mainParam.folderId = this.folderId;
      this.createFileVisible = true;
    },
    rowClick(row) {
      this.currentId = row.ID;
    },
    closeCreateDialog() {
      this.createFileVisible = false;
    },
    // 加载表格数据
    searchItem() {
      let _self = this;

      var key = "C_ITEM_TYPE='案卷' and IS_HIDDEN=0 and status='整编'";

      if (_self.inputkey != "") {
        key =key+" and (coding like '%" + _self.inputkey + "%' or title like '%" + _self.inputkey + "%') ";
      }
      _self.mainParam.condition = key;
      _self.mainParam.folderId = _self.folderId;
      _self.$nextTick(() => {
        _self.$refs.mainDataGrid.loadGridData();
      });
    }
  }
};
</script>