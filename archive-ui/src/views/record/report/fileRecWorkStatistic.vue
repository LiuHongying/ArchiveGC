<template>
  <DataLayout>
    <template v-slot:header style="height: auto">
      <el-form :inline="true">
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
            :placeholder="$t('application.startDate')"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleReport()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <el-table
        ref="mainTable"
        :data="tables.mainTable.data"
        :span-method="objectSpanMethod"
        border
        stripe
        size="mini"
        v-loading="loading"
        :height="layout.height"
      >
        <el-table-column type="index" width="30" fixed></el-table-column>
        <el-table-column width="120" prop="date"></el-table-column>
        <el-table-column
          v-for="item in tables.mainTable.columns"
          :key="item.prop"
          v-bind="item"
        >
        </el-table-column>
      </el-table>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
export default {
    name: "fileRecWorkFlow",
    data(){
        return{
          spanArr: [], 

          tables: {
            mainTable: {
              data: [],
              columns: [
                {
                  prop: "quarterOne",
                  label: "第一季度",
                  fixed: true,
                  width: 110,
                },
              ],
            },
          },

        };
    },

    components: {
        DataLayout: DataLayout,
    },

    methods: {
      getSpanArr() {
        let _self = this;

        for(var i=0; i < _self.tables.mainTable.data.length; i++){
          if(i === 0 ){
            _self.spanArr.push(1);
            _self.pos = 0;
          }else{
            if(_self.tables.mainTable.data[i].prop){

            }
          }
        }
      }
    }
}
</script>
<style scoped>
.el-table thead.is-group tr:first-of-type th:first-of-type:before {
  content: "利用方式";
  text-align: center;
  position: absolute;
  width: 152px;
  height: 1px;
  bottom: 30px;
  right: 0;
}

.el-table thead.is-group tr:first-of-type th:first-of-type:after {
  content: "日期";
  text-align: center;
  position: absolute;
  width: 152px;
  top: 10px;
  left: 0;
}

.el-table thead.is-group tr:first-of-type th:first-of-type .cell {
  position: absolute;
  top: 0;
  left: 0;
  width: 152px;
  height: 1px;
  background-color: #ebeef5;
  display: block;
  text-align: center;
  transform: rotate(38deg); /*旋转*/
  transform-origin: top left;
  -ms-transform: rotate(38deg);
  -ms-transform-origin: top left;
  -webkit-transform: rotate(38deg);
  -webkit-transform-origin: top left;
}
</style>