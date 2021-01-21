<template>
  <DataLayout>
    <template v-slot:header></template>
    <template v-slot:main="{layout}">
      <el-row>
        <el-col :span="6">
          <el-input
            v-model="inputkey"
            :placeholder="$t('message.pleaseInput') + $t('application.keyword')"
            @change="searchItem"
            prefix-icon="el-icon-search"
          ></el-input>
        </el-col>

        <el-col :span="3">
          <el-button type="primary" @click="readed()">已阅</el-button>
        </el-col>
      </el-row>
      <el-row>
        <DataGrid
          ref="mainDataGrid"
          key="main"
          data-url="/dc/getDocuments"
          v-bind:tableHeight="(layout.height-startHeight-75)"
          v-bind:isshowOption="true"
          v-bind:isshowSelection="true"
          gridViewName="DistributionGridApprove"
          :optionWidth="2"
          :condition="condition"
          :isshowCustom="false"
          :isEditProperty="true"
          showOptions
          :isShowChangeList="false"
          :isInitData="true"
          @rowclick="rowClick"
          @selectchange="selectChange"
        >
          <el-dropdown-item
            slot="dropdownItem"
            slot-scope="scope"
            icon="el-icon-reading"
            @click.native="showItemContent(scope.data.row)"
          >{{ $t("application.viewContent") }}</el-dropdown-item>
          <el-button slot="addButton" @click="saveItem()">分发</el-button>
        </DataGrid>
      </el-row>
    </template>
  </DataLayout>
</template>
<script>
import DataGrid from "@/components/DataGrid.vue";
import DataLayout from "@/components/ecm-data-layout";
import CreateDocNoAttach from "@/components/CreateDocNoAttach.vue";
import DistributeRecord from "@/views/archivecd/distribute/DistributeRecord.vue";
export default {
  name: "distributeList",
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    CreateDocNoAttach: CreateDocNoAttach,
    DistributeRecord: DistributeRecord
  },
  data() {
    return {
      condition: " STATUS='新建'",
      inputkey: "",
      selectedItems: [],
      selectedItemList: [],
      selectIds: [],
      currentId: ""
    };
  },
  props: {},
  mounted() {},
  methods: {
    saveItem() {
      let formData = this.$refs.mainDataGrid.$refs.ShowProperty.getFormData();
      let jsonStr = formData.get("metaData");
      let m = JSON.parse(jsonStr);
      axios
        .post("/cd/dc/distributeApprove", JSON.stringify(m))
        .then(function(response) {
          let code = response.data.code;
          //console.log(JSON.stringify(response));
          if (code == 1) {
            _self.$message({
              showClose: true,
              message: "分发成功",
              duration: 2000,
              type: "warning"
            });
            _self.searchItem();
            _self.$emit("onSaved", "update");
            _self.$emit("onSaveSuccess", m);
          } else {
            _self.$message(_self.$t("message.saveFailured"));
          }
        })
        .catch(function(error) {
          _self.$message(_self.$t("message.saveFailured"));
          console.log(error);
        });
    },
    rowClick(row) {
      this.currentId = row.ID;
    },
    // 查看内容
    showItemContent(indata) {
      let distributeId = indata.ID;
      let m = new Map();
      m.set("id", distributeId);
      m.set("condition", " and a.name='文件分发'");
      let _self = this;
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/dc/getAllDocusByRelationParentId"
        })
        .then(function(response) {
          if (response.data.data.length > 0) {
            let docId = response.data.data[0].ID;
            if (docId) {
              let href = _self.$router.resolve({
                path: "/viewdoc",
                query: {
                  id: docId
                  //token: sessionStorage.getItem('access-token')
                }
              });
              //console.log(href);
              window.open(href.href, "_blank");
            } else {
              _self.$message({
                showClose: true,
                message: "文件打开失败！",
                duration: 2000,
                type: "error"
              });
            }
          } else {
            _self.$message({
              showClose: true,
              message: "没有文件，请检查分发单！",
              duration: 2000,
              type: "error"
            });
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    readed() {
      let _self = this;
      if (_self.selectIds.length == 0) {
        _self.$message({
          showClose: true,
          message: "请选择一条或多条数据！",
          duration: 2000,
          type: "warning"
        });
        return;
      }
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(_self.selectIds),
          url: "/cd/dc/readedDistribution"
        })
        .then(function(response) {
          if (response.data.code == "1") {
            _self.$message({
              showClose: true,
              message: "操作成功！",
              duration: 2000,
              type: "success"
            });
            _self.$refs.mainDataGrid.loadGridData();
          } else {
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "error"
            });
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    selectChange(selection) {
      this.selectedItems = selection;
      this.selectedItemList = selection;
      let _self = this;
      if (selection.length == 0) {
        _self.selectIds = [];
      } else {
        selection.forEach(e => {
          _self.selectIds.push(e.ID);
        });
      }
    },
    //搜索
    searchItem() {
      let _self = this;
      let key = _self.inputkey;
      _self.condition = " STATUS='新建'";
      if (key != "") {
        let c =
          " CODING like '%" +
          key +
          "%' or NAME like '%" +
          key +
          "%' or C_FROM_CODING like '%" +
          key +
          "%'";
        _self.condition += " and (" + c + ")";
      }
      _self.$nextTick(() => {
        _self.$refs.mainDataGrid.loadGridData();
      });
    }
  }
};
</script>
<style scoped>
</style>