<template>
  <DataLayout>
    <template v-slot:header style="height: auto">
      <el-form :inline="true">
        <el-form-item>
          <el-date-picker
            v-model="yearS"
            type="year"
            placeholder="选择年"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"  plain @click="handleReport()">{{
            $t("application.SearchData")
          }}</el-button>
        </el-form-item> 
        <el-form-item>
          <el-button type="primary"  plain @click.native="exportStatistic">{{
            $t("application.ExportExcel")
          }}</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template v-slot:main="{ layout }">
      <el-table
        ref="mainTable"
        :data="tables.mainTable.data"
        border
        stripe
        size="mini"
        v-loading="loading"
        :height="layout.height"
      >
        <el-table-column type="index" width="30" fixed></el-table-column>
        <el-table-column
          v-for="item in tables.mainTable.columns"
          :key="item.prop"
          v-bind="item"
        ></el-table-column>
      </el-table>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "avWorkFlow",
  data() {
    return {
      yearS: "",
      loading: false,
      tables: {
        mainTable: {
          data: [],
          columns: [
            {
              prop: "avType",
              label: "文件类型",
              fixed: true,
              width: 110,
            },
            {
              prop: "quarterOne",
              label: "第一季度",
              fixed: true,
              width: 110,
            },
            {
              prop: "quarterTwo",
              label: "第二季度",
              fixed: true,
              width: 110,
            },
            {
              prop: "quarterThree",
              label: "第三季度",
              fixed: true,
              width: 110,
            },
            {
              prop: "quarterFour",
              label: "第四季度",
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
    handleReport() {
      let _self = this;
      _self.loading = true;

      var m = new Map();
      m.set("yearSelect", _self.yearS);

      axios
        .post("/dms/record/AudVidQuarterStatistic", JSON.stringify(m))
        .then(function (response) {
          _self.tables.mainTable.data = response.data.data;
          console.log(_self.tables.mainTable.data);
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    exportStatistic() {
      let _self = this;

      import("@/utils/Export2Excel").then((excel) => {
        let tHeader = [];
        let filterVal = [];
        _self.tables.mainTable.columns.forEach(function (item) {
          tHeader.push(item.label);
          filterVal.push(item.prop);
        });

        const list = _self.tables.mainTable.data;
        const data = this.formatJson(filterVal, list);
        excel.export_json_to_excel({
          header: tHeader,
          data,
          filename: "Report_AV_" + new Date().Format("yyyy-MM-dd"),
        });
      });
    },

    formatJson(filterVal, jsonData) {
      return jsonData.map((v) => filterVal.map((j) => v[j]));
    },
  },
};
</script>
<style scoped>
</style>