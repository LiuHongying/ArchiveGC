<template>
  <div>
    <el-dialog
      title="文件列表"
      :visible.sync="itemDialogVisible"
      width="96%"
      @close="itemDialogVisible = false"
      :append-to-body="true"
    >
      <InnerItemViewer
        ref="innerItemViewer"
        v-bind:id="currentId"
        v-bind:tableHeight="innerTableHeight"
      ></InnerItemViewer>
    </el-dialog>
    <el-dialog
      :title="$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
      width="90%"
      :append-to-body="true"
    >
      <ShowProperty
        ref="ShowProperty"
        @onSaved="onSaved"
        width="100%"
        v-bind:itemId="selectedItemId"
        v-bind:folderId="currentFolder.id"
        v-bind:typeName="currentFolder.typeName"
      ></ShowProperty>
      <div slot="footer" class="dialog-footer">
        <el-button @click="propertyVisible = false">{{
          $t("application.cancel")
        }}</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="选择需要展示的字段"
      :visible.sync="columnsInfo.dialogFormVisible"
      width="40%"
      center
      top="15vh"
      :close-on-click-modal="false"
      :append-to-body="true"
    >
      <el-checkbox
        :indeterminate="columnsInfo.isIndeterminate"
        v-model="columnsInfo.checkAll"
        @change="handleCheckAllChange"
        >全选</el-checkbox
      >
      <el-checkbox-group v-model="showFields" @change="handleCheckedColsChange">
        <el-checkbox
          v-for="item in gridList"
          :label="item.attrName"
          :key="item.attrName"
          >{{ item.label }}</el-checkbox
        >
      </el-checkbox-group>
      <div slot="footer" class="dialog-footer">
        <el-button
          @click="columnsInfo.dialogFormVisible = false"
          size="medium"
          >{{ $t("application.cancel") }}</el-button
        >
        <el-button type="primary" @click="confirmShow" size="medium"
          >确定</el-button
        >
      </div>
    </el-dialog>

    <div :style="{ position: 'relative', height: asideHeight + 'px' }">
      <split-pane
        split="vertical"
        @resize="resize"
        min-percent="10"
        :default-percent="15"
      >
        <template slot="paneL">
          <el-breadcrumb style="padding-top: 10px; padding-bottom: 10px">
            <el-breadcrumb-item class="title16">
              <i class="el-icon-receiving"></i>
              &nbsp; {{ $t("route.companyDoc") }}
            </el-breadcrumb-item>
          </el-breadcrumb>

          <el-container
            :style="{
              height: treeHeight + 'px',
              width: asideWidth,
              overflow: 'auto',
            }"
          >
            <el-tree
              style="width: 100%"
              :props="defaultProps"
              :data="dataList"
              node-key="id"
              :render-content="renderContent"
              default-expand-all
              highlight-current
              @node-click="handleNodeClick"
            ></el-tree>
          </el-container>
        </template>
        <template slot="paneR">
          <el-form :inline="true" @submit.native.prevent>
            <el-form-item>
              <el-input
                v-model="inputkey"
                :placeholder="
                  $t('message.pleaseInput') + $t('application.keyword')
                "
                @change="searchItem"
                prefix-icon="el-icon-search"
              ></el-input>
            </el-form-item>
            <el-form-item v-if="ShowFileType">
                <el-select v-model="DCtype" :placeholder="DCtype.value" v-on:change="changeType()">
                  <el-option
                    v-for="item in DCoptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.label"
                  ></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <!-- `checked` 为 true显示卷宗 或 false不显示卷宗 -->
                <el-checkbox
                  v-model="showBox"
                  @change="showFileBox"
                  >显示案卷</el-checkbox
                >
            </el-form-item>
          </el-form>
          <el-row>
            <el-table
            ref="table"
              :height="tableHeight"
              :data="itemDataList"
              border
              v-loading="tableLoading"
              @selection-change="selectChange"
              @sort-change="sortchange"
              style="width: 100%"
              @header-dragend="onHeaderDragend"
              fit
            >
              <el-table-column
                type="selection"
                @selection-change="selectChange"
                width="50"
              ></el-table-column>
              <el-table-column :label="$t('field.indexNumber')" width="70">
                <template slot-scope="scope">
                  <span>{{
                    (currentPage - 1) * pageSize + scope.$index + 1
                  }}</span>
                </template>
              </el-table-column>
              <el-table-column width="40">
                <template slot-scope="scope">
                  <img
                    v-if="scope.row.TYPE_NAME == '图册'"
                    :src="'./static/img/drawing.gif'"
                    :title="scope.row.TYPE_NAME"
                    border="0"
                  />
                  <img
                    v-else-if="scope.row.C_ITEM_TYPE == '案卷'"
                    :src="'./static/img/box.gif'"
                    :title="scope.row.TYPE_NAME"
                    border="0"
                  />
                  <img
                    v-else-if="
                      scope.row.FORMAT_NAME == null ||
                      scope.row.FORMAT_NAME == ''
                    "
                    :src="'./static/img/format/f_undefined_16.gif'"
                    title="无电子文件"
                    border="0"
                  />
                  <img
                    v-else
                    :src="
                      './static/img/format/f_' +
                      scope.row.FORMAT_NAME +
                      '_16.gif'
                    "
                    :title="scope.row.FORMAT_NAME"
                    border="0"
                  />
                </template> </el-table-column
              >>
              <div v-for="(citem, idx) in gridList" :key="idx">
                <div v-if="citem.visibleType == 1">
                  <el-table-column
                    v-if="(citem.width + '').indexOf('%') > 0"
                    :label="citem.label"
                    :prop="citem.attrName"
                    :min-width="citem.width"
                    :sortable="citem.allowOrderby"
                  >
                    <template slot-scope="scope">
                      <div v-if="citem.attrName.indexOf('DATE') > 0">
                        <span>{{ dateFormat(scope.row[citem.attrName]) }}</span>
                      </div>
                      <div v-else>
                        <span @click="rowClick(scope.row)">{{
                          scope.row[citem.attrName]
                        }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column
                    v-else
                    :label="citem.label"
                    :prop="citem.attrName"
                    :width="citem.width"
                    :sortable="citem.allowOrderby"
                  >
                    <template slot-scope="scope">
                      <div v-if="citem.attrName.indexOf('DATE') > 0">
                        <span>{{ dateFormat(scope.row[citem.attrName]) }}</span>
                      </div>
                      <div v-else>
                        <span @click="rowClick(scope.row)">{{
                          scope.row[citem.attrName]
                        }}</span>
                      </div>
                    </template>
                  </el-table-column>
                </div>
              </div>
              <el-table-column align="left" width="140">
                <template slot="header">
                  <el-button
                    icon="el-icon-s-grid"
                    size="small"
                    @click="dialogFormShow"
                    title="选择展示字段"
                  ></el-button>
                </template>
                <template slot-scope="scope">
                  <el-button
                    type="primary"
                    plain
                    size="small"
                    :title="$t('application.viewContent')"
                    icon="el-icon-picture-outline"
                    @click="showItemContent(scope.row)"
                  ></el-button>
                  <el-button
                    type="primary"
                    plain
                    size="small"
                    :title="$t('application.property')"
                    icon="el-icon-info"
                    @click="showItemProperty(scope.row)"
                  ></el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              background
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-sizes="[10, 20, 50, 100, 200]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="itemCount"
            ></el-pagination>
          </el-row>
        </template>
      </split-pane>
    </div>
  </div>
</template>
<script>
import ShowProperty from "@/components/ShowProperty";
import InnerItemViewer from "@/views/dc/InnerItemViewer.vue";
export default {
  components: {
    ShowProperty: ShowProperty,
    InnerItemViewer: InnerItemViewer,
  },
  data() {
    return {
      currentuser: {
        user: {},
        roles: [],
      },
      columnsInfo: {
        checkAll: true,
        dialogFormVisible: false,
        isIndeterminate: false,
      },
      innerTableHeight: 550 - 360,
      tableHeight: 550 - 170,
      asideHeight: 550 - 100,
      treeHeight: 550 - 130,
      asideWidth: "100%",
      currentLanguage: "zh-cn",
      propertyVisible: false,
      borrowVisible: false,
      loading: false,
      tableLoading: false,
      currentFolder: [],
      isFileAdmin: false,
      showFields: [],
      pageSize: 20,
      dataList: [],
      gridList: [],
      itemDataList: [],
      itemDataListFull: [],
      showBox: false,
      orderBy: "",
      itemCount: 0,
      inputkey: "",
      currentPage: 1,
      selectedItemId: "",
      defaultProps: {
        children: "children",
        label: "name",
      },
      selectedItemList: [],
      itemDialogVisible: false,
      currentId: "",
      disable: true,
      exportAble: false,
      formLabelWidth: "100px",
      borrowData: [],
      shopingCartDialogVisible: false,
      defaultData: {
        gridView: "GeneralGrid",
      },
      gridViewTrans: "",
      idTrans: "",
      DCoptions: [
        {
          value: "all",
          label: "所有文档",
        },
        {
          value: "deline",
          label: "到期文档",
        },
      ],
      DCtype:"所有文档",
      dataUrl:"/dc/getDocuments",
      conditionDC:"",
    };
  },
  created() {
    var username = sessionStorage.getItem("access-userName");
    let _self = this;
    axios.post("/user/getGroupByUserName", username).then(function (response) {
      var groupList = response.data.data;
      groupList.forEach(function (val, index, arr) {
        if (val.name == "档案管理员") {
          _self.isFileAdmin = true;
        }
      });
    });
  },
  mounted() {
    let _self = this;

    var psize = localStorage.getItem("docPageSize");
    if (psize) {
      _self.pageSize = parseInt(psize);
    }
    _self.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    _self.loading = true;
    axios
      .post("/admin/getFoldersByPath", _self.folderPath)
      .then(function (response) {
        _self.dataList = response.data.data;
        _self.loadGridInfo(_self.defaultData);
        _self.loading = false;
      })
      .catch(function (error) {
        console.log(error);
        _self.loading = false;
      });
    
  },
  props: {
    conditionFile: {type:String,default:""},//文件判断
    ShowFileType:{type:Boolean,default:false},//选择文件类型（所有&到期）
    condition:{type:String ,default:""},
    folderPath:{typr:String,default:"/档案库"}
  },
  methods: {
    init(){
    this.selectedItemList = []
    this.$refs.table.clearSelection()
    },
    changeType:function(){
        var nowDate = new Date().getTime();
        if(this.DCtype=="到期文档"){
            this.dataUrl="/dc/getDocuments4DC"
            this.conditionDC="C_INCLUDE_PAPER='有' and IS_RELEASED=1"+this.conditionFile
        }else{
            this.dataUrl="/dc/getDocuments"
            this.conditionDC="C_INCLUDE_PAPER='有' and IS_RELEASED=1 AND IS_CHILD=0 AND IS_CURRENT=1"+this.conditionFile
        }
        setTimeout(() => {
            this.searchItem()
        }, 10);
      
    },
    resize() {
      //console.log('resize')
      this.asideWidth = "100%";
    },
    // 加载表格样式
    initGridInfo() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", "GeneralGrid");
      m.set("lang", _self.currentLanguage);
      axios
        .post("/dc/getGridViewInfo", JSON.stringify(m))
        .then(function (response) {
          _self.showFields = [];
          _self.gridList = response.data.data;
          _self.gridList.forEach((element) => {
            if (element.visibleType == 1) {
              _self.showFields.push(element.attrName);
            }
          });
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    rowClick(row) {
      this.currentId = row.ID;
      if (row.TYPE_NAME == "卷盒" || row.C_ITEM_TYPE == "案卷") {
        this.itemDialogVisible = true;
        let _self = this;
        setTimeout(() => {
          _self.$refs.innerItemViewer.id = _self.currentId;
          _self.$refs.innerItemViewer.loadGridInfo();
          //_self.$refs.innerItemViewer.bindData();
        }, 100);
      }
    },
    renderContent: function (h, { node, data, store }) {
      if (data.extended) {
        return (
          <span>
            <i class="el-icon-folder-opened"></i>
            <span style="padding-left: 4px;">{node.label}</span>
          </span>
        );
      } else {
        return (
          <span>
            <i class="el-icon-folder"></i>
            <span style="padding-left: 4px;">{node.label}</span>
          </span>
        );
      }
    },
    // 查看内容
    showItemContent(indata) {
      let condition = indata.ID;
      let href = this.$router.resolve({
        path: "/viewdoc",
        query: {
          id: condition,
          //token: sessionStorage.getItem('access-token')
        },
      });
      //console.log(href);
      window.open(href.href, "_blank");
    },
    handleNodeClick(indata) {
      let _self = this;
      _self.disable = false;
      _self.exportAble = true;
      _self.currentFolder = indata;
      if (indata.extended == false) {
        _self.loading = true;
        axios
          .post("/admin/getFolder", indata.id)
          .then(function (response) {
            indata.children = response.data.data;
            //console.log(JSON.stringify(indata));
            _self.inputkey = "";
            indata.extended = true;
            _self.loading = false;
          })
          .catch(function (error) {
            console.log(error);
            _self.loading = false;
          });
      }
      _self.loadGridInfo(indata);
      if (_self.showBox) {
        _self.loadAllGridData(indata);
      } else {
        _self.loadGridData(indata);
      }
    },
    showOrHiden(b) {
      this.shopingCartDialogVisible = b;
    },
    sortchange(column) {
      this.orderBy =
        column.column.property + column.column.order == "ascending"
          ? " ASC"
          : " DESC";
    },
    // 加载表格样式
    loadGridInfo(indata) {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", indata.gridView);
      m.set("lang", _self.currentLanguage);
      axios
        .post("/dc/getGridViewInfo", JSON.stringify(m))
        .then(function (response) {
          _self.showFields = [];
          _self.gridList = response.data.data;
          _self.gridList.forEach((element) => {
            if (element.visibleType == 1) {
              _self.showFields.push(element.attrName);
            }
          });
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    // 加载表格数据
    loadGridData(indata) {
      let _self = this;
      _self.tableLoading = true;
      var key = _self.sqlStringFilter(_self.inputkey);
      if(_self.inputkey!=""&&_self.inputkey!=undefined){
        key = "CODING LIKE '%"+key+"%' or TITLE like '%"+key+"%'"
      }
      if(this.ShowFileType){
        if(key!=''&&key!=undefined){
          key+=" and "
        }
        if(_self.conditionDC==""||_self.conditionDC==undefined){
          _self.conditionDC="C_INCLUDE_PAPER='有' and IS_RELEASED=1 AND IS_CHILD=0 AND IS_CURRENT=1"+_self.conditionFile
        }
        key +=_self.conditionDC
      }
      
      if(_self.condition==""||_self.condition==undefined){
        _self.condition="IS_RELEASED=1 "
      }
      if(key!=''&&key!=undefined){
        key+=" and "
      }
      key =key+"C_ITEM_TYPE <>'案卷' AND "+_self.condition
      
      var m = new Map();
      _self.gridViewTrans = indata.gridView;
      _self.idTrans = indata.id;
      m.set("gridName", indata.gridView);
      m.set("folderId", indata.id);
      m.set("condition", key);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", _self.currentPage - 1);
      m.set("orderBy", "MODIFIED_DATE desc");
      axios
        .post(_self.dataUrl, JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemDataListFull = response.data.data;
          _self.itemCount = response.data.pager.total;
          _self.tableHeight = 550 - 170;
          //console.log(JSON.stringify(response.data.datalist));
          _self.tableLoading = false;
        });
    },
    //获取包含卷盒在内的所有信息
    loadAllGridData(indata) {
      let _self = this;
      var key = _self.sqlStringFilter(_self.inputkey);
      if(_self.inputkey!=""&&_self.inputkey!=undefined){
        key = "CODING LIKE '%"+key+"%' "
      }
      if(this.ShowFileType){
        if(key!=''&&key!=undefined){
          key+=" and "
        }
        if(_self.conditionDC==""||_self.conditionDC==undefined){
          _self.conditionDC="C_INCLUDE_PAPER='有' and IS_RELEASED=1 AND IS_CHILD=0 AND IS_CURRENT=1"+_self.conditionFile
        }
        key +=_self.conditionDC
      }
      
      if(_self.condition==""||_self.condition==undefined){
        _self.condition="IS_RELEASED=1 "
      }
      if(key!=''&&key!=undefined){
        key+=" and "
      }
      key =key+_self.condition
      var m = new Map();
      m.set("gridName", indata.gridView);
      m.set("folderId", indata.id);
      m.set("condition", key);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", _self.currentPage - 1);
      m.set("orderBy", "MODIFIED_DATE desc");
      axios
        .post(_self.dataUrl, JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemDataListFull = response.data.data;
          _self.itemCount = response.data.pager.total;
          //console.log(JSON.stringify(response.data.datalist));
          _self.loading = false;
        });
    },
    selectChange(selection) {
      this.selectedItemList = [];
      if (selection.length > 0) {
        for (var i = 0; i < selection.length; i++) {
          this.selectedItemList.push(selection[i]);
        }
      }
      this.$emit("selectchange",this.selectedItemList);
    },
    //展示勾选弹框
    dialogFormShow() {
      this.columnsInfo.dialogFormVisible = true;
    },
    //弹框中全选，展示所有属性
    handleCheckAllChange(val) {
      this.showFields = [];
      if (val) {
        this.gridList.forEach((element) => {
          this.showFields.push(element.attrName);
        });
      }
      this.columnsInfo.isIndeterminate = false;
    },
    //单个勾选属性
    handleCheckedColsChange(value) {
      let checkedCount = value.length;
      this.columnsInfo.checkAll = checkedCount === this.gridList.length;
      this.columnsInfo.isIndeterminate =
        checkedCount > 0 && checkedCount < this.gridList.length;
    },
    //确认按钮显示哪些属性
    confirmShow() {
      let _self = this;
      _self.gridList.forEach((element) => {
        element.visibleType = 2;
      });
      _self.showFields.forEach((element) => {
        let item = _self.getgriditem(element);
        if (item) {
          item.visibleType = 1;
        }
      });
      this.columnsInfo.dialogFormVisible = false;
    },
    getgriditem(attrName) {
      let _self = this;
      let ret = null;
      _self.gridList.forEach((element) => {
        if (element.attrName == attrName) {
          ret = element;
          return;
        }
      });
      return ret;
    },
    //搜索
    searchItem() {
      let _self = this;
      _self.loadGridInfo(_self.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(_self.currentFolder);
      } else {
        _self.loadGridData(_self.currentFolder);
      }
    },

    closeDialog(val) {
      this.borrowVisible = val;
    },
    //查看属性
    showItemProperty(indata) {
      let _self = this;
      _self.selectedItemId = indata.ID;
      _self.propertyVisible = true;
      _self.itemDialogVisible = false;
      if (_self.$refs.ShowProperty) {
        _self.$refs.ShowProperty.myItemId = indata.ID;
        _self.$refs.ShowProperty.loadFormInfo();
      }
    },
    showFileBox() {
      if (this.showBox) {
        this.loadAllGridData(this.currentFolder);
      } else {
        this.loadGridData(this.currentFolder);
      }
    },
    //分页 页数改变
    handleSizeChange(val) {
      let _self = this;
      this.pageSize = val;
      localStorage.setItem("docPageSize", val);
      _self.loadGridInfo(this.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(this.currentFolder);
      } else {
        _self.loadGridData(this.currentFolder);
      }
    },
    // 分页 当前页改变
    handleCurrentChange(val) {
      let _self = this;
      this.currentPage = val;
      _self.loadGridInfo(this.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(this.currentFolder);
      } else {
        _self.loadGridData(this.currentFolder);
      }
    },
    // on save todo
    onSaved(val) {
      let _self = this;
      this.currentPage = val;
      _self.loadGridInfo(this.currentFolder);
      if (_self.showBox) {
        _self.loadAllGridData(this.currentFolder);
      } else {
        _self.loadGridData(this.currentFolder);
      }
    },

    exportData() {
      let _self = this;
      let params = {
        URL: "/file/exportFolderPath",
        gridName: _self.currentFolder.gridView,
        folderId: _self.currentFolder.id,
        orderBy: "MODIFIED_DATE desc",
        pageSize: _self.pageSize,
        pageIndex: (_self.currentPage - 1) * _self.pageSize,
        lang: "zh-cn",
      };
      console.log(params);
      ExcelUtil.export4Cnpe(params);
    },
  },
};
</script>
<style scoped>
.el-container {
  height: 100%;
}
/* .el-aside {
  height: 680px;
} */
</style>
<style>
</style>