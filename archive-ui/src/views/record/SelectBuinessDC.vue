<template>
    <DataLayout>
    <template v-slot:header>
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编码或标题'
            @keyup.enter.native="search()"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
        <el-form-item>
          <AddCondition v-model="AdvCondition" :inputType="hiddenInput" @sendMsg="search()"></AddCondition>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <DataGrid
        ref="InvalidGrid"
        key="Invalid"
        dataUrl="/dc/getDocuments"
        v-bind:tableHeight="layout.height-86"
        v-bind:isshowOption="true" v-bind:isshowSelection ="false"
        gridViewName="BusinessSelectGrid"
        condition="IS_RELEASED=1"
        :optionWidth = "2"
        :isshowCustom="false"
        :isEditProperty="false"
        :isShowMoreOption="true"
        showOptions="查看内容"
        :isshowicon="true"
        :isShowChangeList="false"
        :isInitData="false"
      >
      </DataGrid>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import DataGrid from "@/components/DataGrid";
import DataLayout from "@/components/ecm-data-layout";
import AddCondition from '@/views/record/AddCondition'
export default {
  data() {
    return {
     
      inputValueNum:"",
      typename:"'供应商管理文件','招标文件','投标文件','商务过程文件','合同管理文件','其他采购文件'",
      AdvCondition:"",
      hiddenInput:"hidden",
    };
  },
  mounted() {
    this.search();
  },
  methods: {
    //文档模糊查询
    search() {
      let _self = this;
      let key=" TYPE_NAME IN("+_self.typename+") AND IS_RELEASED=1";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%')";
      }
      if (_self.AdvCondition != "" && _self.AdvCondition != undefined) {
        key +=
          " and "+ _self.AdvCondition 
          _self.AdvCondition=''
      }
      _self.$refs.InvalidGrid.condition=key;
      _self.$refs.InvalidGrid.currentPage = 1;
      _self.$refs.InvalidGrid.loadGridInfo();
      _self.$refs.InvalidGrid.loadGridData();
    },
  },
  props: {},
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    AddCondition:AddCondition,
  },
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 0px;
}
.el-table td,
.el-table th {
  text-align: center !important;
}
</style>