<template>
  <DataLayout>
    <template v-slot:header>
      <el-form :inline="true" :model="filters" @submit.native.prevent>
        <el-form-item>
          <el-input
            v-model="filters.title"
            :placeholder="$t('application.Title')"
            @keyup.enter.native="searchItem"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" v-on:click="searchItem">{{$t('application.SearchData')}}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{layout}">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <!--列表-->
          <DataGrid
            ref="draftList"
            key="draftList"
            data-url="/dc/getDocuments"
            v-bind:tableHeight="(layout.height-startHeight)*topPercent/100"
            v-bind:isshowOption="true"
            v-bind:isshowSelection="true"
            gridViewName="WorkflowFileGrid"
            :condition="searchFileCondition"
            :optionWidth="1"
            :isShowMoreOption="false"
            :isshowCustom="false"
            :isEditProperty="false"
            :isShowChangeList="false"
            :isshowicon="false"
            @selectchange="fileSelect"
          ></DataGrid>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
import DataGrid from "@/components/DataGridNpc";
import AddCondition from "@/views/record/AddCondition";
import RejectButton from "@/components/RejectButton";
import ExcelUtil from "@/utils/excel.js";
import DataSelect from "@/components/ecm-data-select";
import DataLayout from "@/components/ecm-data-layout";
import AttachmentFile from "@/views/dc/AttachmentFile.vue";
export default {
  name: "Drafts",
  components: {
    ShowProperty: ShowProperty,
    DataGrid: DataGrid,
    AddCondition: AddCondition,
    DataSelect: DataSelect,
    RejectButton: RejectButton,
    DataLayout: DataLayout,
    AttachmentFile: AttachmentFile
  },
  model: {
    event: "change"
  },
  props: {
    allowEdit: { type: Boolean, default: true },
    isShowPage: { type: Boolean, default: true },
    parentId: { type: String, default: "" },
    processDefinitionId: { type: String, default: "" },
    activityName: { type: String, default: "" }
  },
  data() {
    return {
      // 本地存储高度名称
      topStorageName: "ReceivedDCHeight",
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 90,
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
      selectedFiles: [],
      butt: false,
      searchFileCondition: ""
    };
  },
  mounted() {
    
  },
  methods: {
    searchItem() {
      let _self = this;
      
      // let key = " status='新建' and C_ITEM_TYPE='表单' and CREATOR='@currentuser' ";
      let key = " status='新建' and CREATOR='@currentuser' ";
      if (_self.filters.title != "") {
        key +=" and TITLE like '%" +
          _self.filters.title +
          "%' "
      }
      if (key != "") {
        _self.searchFileCondition = key;
      }
      _self.$refs.draftList.loadGridData();
    },
    
    fileSelect(val) {
      this.selectedFiles = val;
    },
    
    
    
  }
};
</script>
<style scoped>
</style>