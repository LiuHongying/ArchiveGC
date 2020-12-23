<template>
  <DataLayout>
    <template v-slot:header>
      <el-form :inline="true">
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编码或标题'
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <div :style="{position:'relative',height: layout.height-startHeight+'px'}">
        <split-pane v-on:resize="onSplitResize" :min-percent='20' :default-percent='topPercent' split="horizontal">
          <template slot="paneL">
                <DataGrid
                  ref="ThematicGrid"
                  key="Thematic"
                  dataUrl="/dc/getDocuments"
                  v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
                  v-bind:isshowOption="true" v-bind:isshowSelection ="false"
                  gridViewName="ThematicManagementGrid"
                  condition="TYPE_NAME='专题' and STATUS='新建'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isEditProperty="false"
                  showOptions="查看内容"
                  :isShowChangeList="false"
                  @rowclick="onDataGridRowClick"
                >
                </DataGrid>
          </template>
          <template slot="paneR">
            <DataGrid
              ref="Drawing"
              key="Drawing"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
              v-bind:isshowOption="true" v-bind:isshowSelection ="false"
              gridViewName="GeneralGrid"
              :isInitData="false"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="false"
              showOptions="查看内容"
              :isShowChangeList="false"
            >
            </DataGrid>
          </template>
        </split-pane>
      </div>
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
      // 本地存储高度名称
      topStorageName: 'ReceivingDC4CnpeHeight',
      // 非split pan 控制区域高度
      startHeight: 135,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight:35,
      inputValueNum:"",
    };
  },
  mounted() {
  },
  methods: {
    // 上下分屏事件
    onSplitResize(topPercent){
      // 顶部百分比*100
      this.topPercent = topPercent
      this.setStorageNumber(this.topStorageName, topPercent)
      //console.log(JSON.stringify(topPercent))
    },
    //单击行
    onDataGridRowClick: function (row) {
      var condition1 =
        "SELECT CHILD_ID from ecm_relation where NAME='专题'and PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ") AND IS_RELEASED=1 and STATUS <> '作废'";
      this.$refs.Drawing.condition = key1;
      this.$refs.Drawing.gridViewName = "GeneralGrid";
      this.$refs.Drawing.loadGridInfo();
      this.$refs.Drawing.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME='专题'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR TITLE LIKE '%"+_self.inputValueNum+"%')";
      }
      _self.$refs.ThematicGrid.condition=key;
      _self.$refs.ThematicGrid.currentPage = 1;
      _self.$refs.ThematicGrid.loadGridInfo();
      _self.$refs.ThematicGrid.loadGridData();
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