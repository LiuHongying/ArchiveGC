<template>
  <div>
    <el-dialog
      title="分发记录"
      :visible.sync="createFileVisible"
      @close="createFileVisible = false"
      width="90%"
      style="width:100%"
      :close-on-click-modal="false"
      v-dialogDrag
    >
      <DataLayout>
        <template v-slot:header></template>
        <template v-slot:main="{layout}">
          <el-row>
            <DataGrid
              ref="mainDataGrid"
              key="main"
              data-url="/cd/dc/getDistributeRecord"
              v-bind:tableHeight="(layout.height-startHeight-75)"
              v-bind:isshowOption="true"
              v-bind:isshowSelection="true"
              gridViewName="DistributionGrid"
              :optionWidth="2"
              :condition="condition"
              :isshowCustom="false"
              :isEditProperty="false"
              :isShowMoreOption="false"
              :isShowChangeList="false"
              :isInitData="false"
              :extparam="param"
              @rowclick="rowClick"
              @selectchange="selectChange"
            ></DataGrid>
          </el-row>
        </template>
      </DataLayout>
    </el-dialog>
    <el-button @click="showRecord" type="primary">
      <slot>查看分发记录</slot>
    </el-button>
  </div>
</template>
<script>
import DataGrid from "@/components/DataGrid.vue";
import DataLayout from "@/components/ecm-data-layout";
export default {
  name: "DistributeRecord",
  components: {
    DataGrid: DataGrid,
    DataLayout: DataLayout
  },
  data() {
    return {
      startHeight: 135,
      createFileVisible: false,
      condition: " ",
      param: new Map(),
    };
  },
  props: {
    distributeId: { type: String, default: null },
    fileId: { type: String, default: null }
  },
  mounted() {},
  methods: {
    showRecord() {
      let _self = this;
      if (_self.distributeId == null && _self.fileId == null) {
        _self.$message({
          showClose: true,
          message: "请选择一条分发单或文件！",
          duration: 2000,
          type: "error"
        });
        return;
      }
      if (_self.distributeId != null) {
        _self.param.set("distributeId", _self.distributeId);
      }
      if (_self.fileId != null) {
        _self.param.set("fileId", _self.fileId);
      }

      _self.createFileVisible = true;
      _self.$nextTick(() => {
        _self.$refs.mainDataGrid.loadGridData();
      });
    }
  }
};
</script>
<style scoped>
</style>












