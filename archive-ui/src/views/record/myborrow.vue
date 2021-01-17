<template>
  <DataLayout>
    <template v-slot:header>
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入借阅单号'
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="startDate"
            type="date"
            :placeholder="$t('application.startDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="endDate"
            type="date"
            align="right"
            :placeholder="$t('application.endDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-select v-model="value" placeholder="待入库" v-on:change="changeStatus(val)">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.label"
            ></el-option>
          </el-select>
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
                  v-bind:isshowOption="true" v-bind:isshowSelection ="true"
                  gridViewName="BorrowListGrid"
                  condition="STATUS='待入库' and  CREATOR ='@currentuser'"
                  :optionWidth = "2"
                  :isshowCustom="false"
                  :isShowMoreOption="false"
                  :isshowicon="false"
                  :isEditProperty="false"
                  :isShowChangeList="false"
                  @selectchange="selectThChange"
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
              v-bind:isshowOption="true" v-bind:isshowSelection ="true"
              gridViewName="BorrowFileListGrid"
              :isInitData="false"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="false"
              showOptions="查看内容"
              :isShowChangeList="false"
              @selectchange="selectDCChange"
            >
            </DataGrid>
          </template>
        </split-pane>
      </div>
    </template>
  </DataLayout>
</template>
<script type="text/javascript">
import ShowProperty from "@/components/ShowProperty";
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
      bottomHeight: 35,
      selectedDCItems: [],//文档
      selectedThItems:[],//专题
      inputValueNum:"",
      options: [
        {
          value: "待入库",
          label: "待入库",
        },
        {
          value: "流程中",
          label: "流程中",
        },
        {
          value: "待出库",
          label: "待出库",
        },
        {
          value: "已完成",
          label: "已完成",
        },
      ],
      value:"待入库",
      startDate: "",
      endDate:"",
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
        "SELECT CHILD_ID from ecm_relation where PARENT_ID ='" +row.ID +"'";
      var key1 = "ID IN (" + condition1 + ")";
      this.$refs.Drawing.condition = key1;
      this.$refs.Drawing.gridViewName = "GeneralGrid";
      this.$refs.Drawing.loadGridInfo();
      this.$refs.Drawing.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME='借阅单' and CREATOR ='@currentuser'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' )";
      }
      if(_self.value!=''&&_self.value!=undefined){
        key+=" and STATUS = '"+_self.value+"'";
      }
      if(_self.startDate!=''&&_self.startDate!=undefined){
          key+=" and CREATION_DATE > '"+_self.startDate+"'";
      }
      if(_self.endDate!=''&&_self.endDate!=undefined){
          key+=" and CREATION_DATE < '"+_self.endDate+"'";
      }
      _self.$refs.ThematicGrid.condition=key;
      _self.$refs.ThematicGrid.currentPage = 1;
      _self.$refs.ThematicGrid.loadGridInfo();
      _self.$refs.ThematicGrid.loadGridData();
    },
    selectDCChange(val) {
      this.selectedDCItems = val;
    },
    selectThChange(val) {
      this.selectedThItems = val;
    },
    changeStatus:function(){
      this.search()
    },
  },
  props: {},
  components: {
    ShowProperty:ShowProperty,
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