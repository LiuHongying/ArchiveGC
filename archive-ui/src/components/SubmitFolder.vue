<template>
  <div>
    <el-button type="primary" @click="submitWhouse()">提交入库</el-button>
  </div>
</template>
<script type="text/javascript">
export default {
  name: "ArchieveStorage",
  data() {
    return {};
  },
  props: {
    selectRowData: { type: Array, default: [] },
  },
  mounted() {},
  methods: {
    submitWhouse() {
      let _self = this;
      var m = [];
      let tab = _self.selectRowData;
      
      if (_self.selectRowData.length == 0) {
        _self.$message({
          showClose: true,
          message: _self.$t("message.pleaseSelectDC"),
          duration: 2000,
          type: "warning",
        });
        return;
      }

      var i;
      for (i = 0; i < tab.length; i++) {
        m.push(tab[i]["ID"]);
      }

      axios
        .post("/record/archiveStorage", JSON.stringify(m), {
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
        })
        .then(function (response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.operationSuccess"),
              duration: 2000,
              type: "success",
            });
            _self.$emit("reloadGrid", "submitReload");
          } else {
            _self.$message({
              showClose: true,
              message: _self.$t("message.operationFaild"),
              duration: 5000,
              type: "error",
            });
          }
        })
        .catch(function (error) {
          _self.$message({
            showClose: true,
            message: _self.$t("message.operationFaild"),
            duration: 5000,
            type: "error",
          });
          console.log(error);
        });
    },
  },
  components: {},
};
</script>
<style scoped>
</style>