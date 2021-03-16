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
            :placeholder="$t('application.endDate')"
            value-format="yyyy-MM-dd"
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
        >
        </el-table-column>
      </el-table>
    </template>
  </DataLayout>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "srWorkFlow",
  data() {
    return {
      startDate: "",
      endDate: "",
      loading: false,
      tables: {
        mainTable: {
          data: [],
          columns: [
            {
              prop: "fileType",
              label: "文件类型",
              fixed: true,
              width: 110,
            },
            {
              prop: "fileCountPre",
              label: "文件数量(册)",
              fixed: true,
              width: 110,
            },
            {
              prop: "cdCountPre",
              label: "光盘个数(张)",
              fixed: true,
              width: 110,
            },
            {
              prop: "changeCountPre",
              label: "调改次数",
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
      m.set("startDate", _self.startDate);
      m.set("endDate", _self.endDate);

      axios
        .post("/dms/record/scientificQuarterStatistic", JSON.stringify(m))
        .then(function (response) {
          _self.tables.mainTable.data = response.data.data;
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
          filename: "Report_Scientific_" + new Date().Format("yyyy-MM-dd"),
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