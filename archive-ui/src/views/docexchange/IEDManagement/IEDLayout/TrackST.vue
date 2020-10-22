<template>
  <div>
    <el-button type="primary" @click="dialogVisible = true">{{$t('route.trackIED')}}</el-button>
    <el-dialog :title="$t('message.conditionS')" :visible.sync="dialogVisible" width="30%">
      <span>
        <el-main v-loading="loading" element-loading-spinner="el-icon-loading">
        <el-form>
          <el-form-item>
            <DataSelect
              v-model="TrackIEDList"
              data-url="/exchange/project/myproject"
              data-value-field="name"
              data-text-field="name"
              includeAll
            ></DataSelect>
          </el-form-item>
          <el-form-item>
            <el-select v-model="value" :placeholder="$t('message.selectDesType')">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.label"
              ></el-option>
            </el-select>
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
        </el-main>
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{$t('application.cancel')}}</el-button>
        <el-button
          type="primary"
          @click="exportAll()" 
          >{{$t('application.ok')}}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script type="text/javascript">
import DataSelect from "@/components/ecm-data-select";
export default {
  name: "IEDTrack",
  data() {
    return {
      options: [
        {
          value: "选项1",
          label: "提交计划",
        },
        {
          value: "选项2",
          label: "外部计划",
        },
      ],
      value: "",

      loading: false,
      TrackIEDList: "",
      startDate: "",
      endDate: "",
      dialogVisible: false,
    };
  },
  mounted() {},
  methods: {
    exportAll() {
      let _self = this;
      _self.loading = true;
      let user = _self.currentUser();
      var fileDate = new Date();
      let fileDateStr =
        fileDate.getFullYear() +
        "" +
        fileDate.getMonth() +
        "" +
        fileDate.getDate();

      var m = new Map();
      m.set("userType", user.userType);
      m.set("userCompany", user.company);
      m.set("iedPlanStatistic", _self.TrackIEDList);
      m.set("planSelect", _self.value);
      m.set("startDate", _self.startDate);
      m.set("endDate", _self.endDate);
      m.set("fileName", "IED_Track_" + fileDateStr + ".xlsx");
      m.set("sheetName", "IED_Track");

      axios
        .post("/exchange/ied/IEDTrackReport", JSON.stringify(m), {
          responseType: "blob",
        })
        .then(function (res) {
          console.log(res);
          let fileName = res.headers["content-disposition"]
            .split(";")[1]
            .split("=")[1]
            .replace(/\"/g, "");
          let type = res.headers["content-type"];
          let blob = new Blob([res.data], { type: type });
          // IE
          if (window.navigator.msSaveBlob) {
            window.navigator.msSaveBlob(blob, fileName);
          } else {
            var link = document.createElement("a");
            link.href = window.URL.createObjectURL(blob);
            link.download = fileName;
            link.click();
            //释放内存
            window.URL.revokeObjectURL(link.href);
          }
          _self.loading = false;
          _self.dialogVisible = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.dialogVisible = false;
        });
      
    },
  },
  components: {
    DataSelect: DataSelect,
  },
};
</script>
<style scoped>
</style>