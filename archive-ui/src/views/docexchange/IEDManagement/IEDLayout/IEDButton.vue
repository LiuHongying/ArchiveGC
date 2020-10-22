<template>
  <div>
    <el-button type="primary" @click="dialogVisible = true">{{$t('route.IEDAllReport')}}</el-button>
    <el-dialog
      :title="$t('message.conditionS')"
      :visible.sync="dialogVisible"
      width="30%"
    >
      <span>
        <el-form>
          <el-form-item>
            <DataSelect
              v-model="allIED"
              data-url="/exchange/project/myproject"
              data-value-field="name"
              data-text-field="name"
              includeAll
            ></DataSelect>
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
        </el-form>
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{$t('application.cancel')}}</el-button>
        <el-button type="primary" @click="exportAll()">{{$t('application.ok')}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script type="text/javascript">
import DataSelect from "@/components/ecm-data-select";
import ExcelUtil from "@/utils/excel.js";
export default {
  name: "IEDAll",
  data() {
    return {
      allIED: "",
      startDate: "",
      endDate: "",
      dialogVisible: false,
    };
  },
  mounted() {},
  methods: {
    exportAll() {
      let _self = this;
      var k =
        "TYPE_NAME='IED' AND IS_CURRENT=1 AND C_IS_RELEASED=1";

      if (_self.startDate != "" && _self.endDate != "") {
        k += "";
      }

      if (
        _self.startDate != undefined &&
        _self.endDate != undefined &&
        _self.startDate != null &&
        _self.endDate != null &&
        _self.startDate.length > 0 &&
        _self.endDate.length > 0
      ) {
        k +=
          " AND (C_ITEM_DATE BETWEEN '" +
          _self.startDate +
          "'" +
          " AND '" +
          _self.endDate +
          "'" +
          ")";
      }

      if (_self.startDate == undefined && _self.endDate != undefined) {
        k += " AND (C_ITEM_DATE < '" + _self.endDate + "'" + ")";
      }

      if (_self.startDate != undefined && _self.endDate == undefined) {
        k += " AND (C_ITEM_DATE > '" + _self.startDate + "'" + ")";
      }

      if (this.allIED != undefined && this.allIED.length > 0) {
        k += " AND C_PROJECT_NAME in (" + _self.allIED + ")";
      }

      let user = this.currentUser();
      if (user.userType == 2 && user.company != null) {
        k += " AND C_COMPANY='" + user.company + "'";
      }

      let dataUrl = "/exchange/doc/export";
      var fileDate = new Date();
      let fileDateStr =
        fileDate.getFullYear() +
        "" +
        fileDate.getMonth() +
        "" +
        fileDate.getDate();
      let params = {
        gridName: "IEDReportGrid",
        lang: "zh-cn",
        condition: k,
        filename: "IED_Report_All_" + fileDateStr + ".xlsx",
        sheetname: "Result",
      };
      ExcelUtil.export(params);
      _self.dialogVisible = false
    },
  },
  components: {
    DataSelect: DataSelect,
  },
};
</script>
<style scoped>
</style>