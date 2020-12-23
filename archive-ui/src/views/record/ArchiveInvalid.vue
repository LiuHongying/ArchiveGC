<template>
    <DataLayout>
    <template v-slot:header>
      <el-form :inline="true">
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编码或标题'
            @keyup.enter.native="search()"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="开始日期-作废日期"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="endDate"
            type="date"
            align="right"
            placeholder="结束日期-作废日期"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <DataGrid
        ref="InvalidGrid"
        key="Invalid"
        dataUrl="/dc/getDocuments"
        v-bind:tableHeight="layout.height-166"
        v-bind:isshowOption="true" v-bind:isshowSelection ="false"
        gridViewName="InvalidGrid"
        condition="STATUS='已完成'"
        :optionWidth = "2"
        :isshowCustom="false"
        :isEditProperty="false"
        :isShowMoreOption="false"
        :isshowicon="false"
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
export default {
  name: "TC",
  data() {
    return {
     
      inputValueNum:"",
      startDate: "",
      endDate:"",
      typename:"'借阅单'",
    };
  },
  mounted() {
    this.search();
  },
  methods: {
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME='设计文件' AND STATUS='已作废'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%')";
      }
      if(_self.startDate!=''&&_self.startDate!=undefined){
          key+=" and C_CANCEL_DATE > '"+_self.startDate+"'";
      }
      if(_self.endDate!=''&&_self.endDate!=undefined){
          key+=" and C_CANCEL_DATE < '"+_self.endDate+"'";
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