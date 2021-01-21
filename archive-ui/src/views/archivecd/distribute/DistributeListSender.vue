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
          <DistributeRecord :distributeId="currentId">分发记录</DistributeRecord>
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
          gridViewName="DistributionGridMysender"
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
        </DataGrid>
      </el-row>
    </template>
  </DataLayout>
</template>
<script>
import DataGrid from "@/components/DataGrid.vue";
import DataLayout from "@/components/ecm-data-layout";
import CreateDocNoAttach from "@/components/CreateDocNoAttach.vue";
import DistributeRecord from "@/views/archivecd/distribute/DistributeRecord.vue"
export default {
  name: "distributeList",
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout,
    CreateDocNoAttach:CreateDocNoAttach,
    DistributeRecord:DistributeRecord
  },
  data() {
    return {
      condition: "",
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
      _self.condition = " ";
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