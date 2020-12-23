<template>
    <DataLayout>
    <template v-slot:header>
      <el-form :inline="true">
        <el-form-item>
          <el-input
            style="width: 200px"
            v-model="inputValueNum"
            placeholder='请输入编码或说明'
            @keyup.enter.native="search()"
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
          <el-row>
            <el-col :span="6" style="padding-left:10px;">借阅人</el-col>
            <el-col :span="18" style="float:left;text-align:left;">
                <el-input
                v-model="orderInputkey"
                placeholder="请输入借阅人"
                @keyup.enter.native="search()"
                ></el-input>
            </el-col>
          </el-row>
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
              ref="completeGrid"
              key="complete"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="(layout.height-startHeight)*topPercent/100-topbarHeight"
              v-bind:isshowOption="true" v-bind:isshowSelection ="false"
              gridViewName="FormGrid"
              condition="STATUS='已完成'"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="false"
              :isShowMoreOption="false"
              :isshowicon="false"
              :isShowChangeList="false"
              :isInitData="false"
              @rowclick="onDataGridRowClick"
            >
            </DataGrid>
          </template>
          <template slot="paneR">
            <DataGrid
              ref="CompGrid"
              key="Comp"
              dataUrl="/dc/getDocuments"
              v-bind:tableHeight="(layout.height-startHeight)*(100-topPercent)/100-bottomHeight"
              v-bind:isshowOption="true" v-bind:isshowSelection ="false"
              gridViewName="FormDcGrid"
              condition="STATUS='已完成'"
              :optionWidth = "2"
              :isshowCustom="false"
              :isEditProperty="false"
              showOptions="查看内容"
              :isShowChangeList="false"
              :isInitData="false"
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
      startHeight: 180,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight:35,
      inputValueNum:"",
      startDate: "",
      endDate:"",
      orderInputkey:"",
      typename:"'借阅单','复制单','设计文件修改单','科研文件借阅单','科研文件修改单'",
    };
  },
  mounted() {
    this.search();
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
      var key1 = "ID IN (" + condition1 + ") AND STATUS='已完成' ";
      this.$refs.CompGrid.condition = key1;
      this.$refs.CompGrid.loadGridInfo();
      this.$refs.CompGrid.loadGridData();
    },
    //文档模糊查询
    search() {
      let _self = this;
      let key="TYPE_NAME IN("+_self.typename+") AND STATUS='已完成'";
      if(_self.inputValueNum!=''&&_self.inputValueNum!=undefined){
        key+="and (CODING LIKE '%"+_self.inputValueNum+"%' OR C_COMMENT LIKE '%"+_self.inputValueNum+"%')";
      }
      if(_self.startDate!=''&&_self.startDate!=undefined){
          key+=" and CREATION_DATE > '"+_self.startDate+"'";
      }
      if(_self.endDate!=''&&_self.endDate!=undefined){
          key+=" and CREATION_DATE < '"+_self.endDate+"'";
      }
      if(_self.orderInputkey!=''&&_self.orderInputkey!=undefined){
          key+=" and C_DRAFTER = '"+_self.orderInputkey+"'";
      }
      _self.$refs.completeGrid.condition=key;
      _self.$refs.completeGrid.currentPage = 1;
      _self.$refs.completeGrid.loadGridInfo();
      _self.$refs.completeGrid.loadGridData();
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