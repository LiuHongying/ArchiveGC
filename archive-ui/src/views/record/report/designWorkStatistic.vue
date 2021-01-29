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
  name: "designWorkFlow",
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
              prop: "wfMonth",
              label: "日期",
              fixed: true,
              width: 110,
            },
            {
              prop: "wfName",
              label: "姓名",
              fixed: true,
              width: 110,
            },
            {
              prop: "receFiles",
              label: "整编归档",
              fixed: true,
              width: 110,
            },
            {
              prop: "receDoc",
              label: "文件修改",
              fixed: true,
              width: 110,
            },
            {
              prop: "storeFiles",
              label: "质检",
              fixed: true,
              width: 110,
            },
            {
              prop: "storeDoc",
              label: "作废",
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
        .post("/dms/record/designStatistic", JSON.stringify(m))
        .then(function (response) {
          _self.tables.mainTable.data = response.data.data;
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },
  },
};
</script>
<style scoped>
</style>