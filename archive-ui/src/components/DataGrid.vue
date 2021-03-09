<template>
  <div>
    <div style="background:#FFFFFF;">
      <!-- 创建分发 -->
      <!--  -->
      <el-dialog
        :append-to-body="true"
        :title="$t('application.editColumn')"
        :visible.sync="editColumn"
        width="80%"
        destroy-on-close
      >
        <EcmCustomColumns
          ref="ecmCustomColumns"
          :gridViewName="gridViewName"
          :archiveInfo="archiveMap"
          @loadMainListConfig = "refreshMainConfigList"
          @onClose="onCloseCustom" @onCancel="editColumn=false"
        >
        </EcmCustomColumns>
      </el-dialog>
      <!-- Matthew changes on 2021年1月27日17:50:47 -->
      <el-dialog 
        title="自定义列表"
        :visible.sync="showConfigList"
        :append-to-body="true"
        width="30%"
        style="height:300px">
        <el-form>
          <el-form-item label="选择配置">
            <el-select v-model="configName">
              <el-option v-for="item in customList" :key="item.id" :label="item.name" :value="item.id" @click.native="changeConfig(item)"></el-option>
            </el-select>
            <el-button type="primary" @click="useConfig">应用</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>
      <!-- end -->
      <el-dialog
        v-dialogDrag
        :append-to-body="true"
        :title="typeName + $t('application.property')"
        :visible.sync="propertyVisible"
        @close="propertyVisible = false"
        width="100%"
        style="text-align: center"
        :close-on-click-modal="false"
      >
        <ShowProperty
          v-if="isEditProperty"
          ref="ShowProperty"
          @onSaved="onSaved"
          @onSaveSuccess="onPropertiesSaveSuccess"
          width="100%"
          :typeName="typeName"
          v-bind:itemId="selectedItemId"
        ></ShowProperty>
        <ShowPropertyReadOnly
          v-else
          ref="ShowProperty"
          width="100%"
          :itemId="selectedItemId"
          :typeName="typeName"
        ></ShowPropertyReadOnly>
        <div slot="footer" class="dialog-footer">
          <el-button v-if="showBatchCheck" @click="onPrevDoc" :disabled="prevButtonDisabled">上一条</el-button>
          <el-button v-if="showBatchCheck" @click="onNextDoc" :disabled="nextButtonDisabled">下一条</el-button>
          <slot name="addButton">

          </slot>
          <slot name="saveButton" :data="propertiesData">
            <el-button v-if="isEditProperty" @click="saveItem()">{{
              $t("application.save")
            }}</el-button>
          </slot>

          <el-button @click="propertyVisible = false">{{
            $t("application.cancel")
          }}</el-button>
        </div>
      </el-dialog>
      <!-- 选字段对话框 -->
      <el-dialog
        :title="$t('application.chooseColumn')"
        :visible.sync="columnsInfo.dialogFormVisible"
        width="40%"
        center
        top="15vh"
        :append-to-body="true"
      >
        <el-checkbox
          :indeterminate="columnsInfo.isIndeterminate"
          v-model="columnsInfo.checkAll"
          @change="handleCheckAllChange"
          >{{ $t("application.selectAll") }}</el-checkbox
        >
        <div style="margin: 15px 0"></div>
        <el-checkbox-group
          v-model="showFields"
          @change="handleCheckedColsChange"
        >
          <el-checkbox
            v-for="item in columnList"
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
          <el-button type="primary" @click="confirmShow" size="medium">{{
            $t("application.ok")
          }}</el-button>
        </div>
      </el-dialog>

      <el-table
        :key="rkey"
        id="datatable"
        ref="datatable"
        :height="tableHeight"
        :data="itemDataList"
        border
        @selection-change="selectChange"
        @sort-change="sortchange"
        @row-click="rowClick"
        @row-dblclick="dbclick"
        @header-dragend="onHeaderDragend"
        v-loading="loading"
        :style="{ width: tableWidth }"
        highlight-current-row
        @cell-mouse-enter="cellMouseEnter"
        @cell-mouse-leave="cellMouseLeave"
        width="100%"
      >
        <el-table-column
          v-if="isshowSelection"
          type="selection"
          width="40"
        ></el-table-column>
        <el-table-column :label="$t('field.indexNumber')" key="#1" width="50">
          <template slot-scope="scope">
            <slot name="sequee" :data="scope" :currentPage="currentPage" :pageSize="pageSize">
              <span>{{ (currentPage - 1) * pageSize + scope.$index + 1 }}</span>
            </slot> 
            <!-- <slot name="sequee" :data="{scope,'currentPage':currentPage,'pageSize':pageSize}">
                <span>{{(currentPage-1) * pageSize + scope.$index+1}}</span>
              </slot> -->
          </template>
        </el-table-column>
        <el-table-column width="40" v-if="isshowicon">
          <template slot-scope="scope">
            <img
              v-if="scope.row.TYPE_NAME == '图册'"
              :src="'./static/img/drawing.gif'"
              :title="scope.row.TYPE_NAME"
              border="0"
            />
            <img
              v-else-if="scope.row.TYPE_NAME == '卷盒' || scope.row.C_ITEM_TYPE == '案卷'"
              :src="'./static/img/box.gif'"
              :title="scope.row.TYPE_NAME"
              border="0"
            />
            <img
              v-else-if="
                scope.row.FORMAT_NAME == null || scope.row.FORMAT_NAME == ''
              "
              :src="'./static/img/format/f_undefined_16.gif'"
              title="无电子文件"
              border="0"
            />
            <img
              v-else
              :src="
                './static/img/format/f_' + scope.row.FORMAT_NAME + '_16.gif'
              "
              :title="scope.row.FORMAT_NAME"
              border="0"
            />
          </template>
        </el-table-column>
        <el-table-column width="60">
         <template slot-scope="scope">
                            <el-button
                              type="primary"
                              plain
                              size="small"
                              :title="$t('application.viewContent')"
                              icon="el-icon-picture-outline"
                              @click="showItemContent(scope.row)"
                            ></el-button>
                          </template>
        </el-table-column>
        <template>
          <template v-for="(citem, idx) in columnList">
            <template v-if="citem.visibleType == 1">
              <template v-if="(citem.width + '').indexOf('%') > 0">
                <el-table-column
                  :label="citem.label"
                  :prop="citem.attrName"
                  :min-width="citem.width"
                  :sortable="sortBackData?'custom':citem.allowOrderby"
                  :key="idx + '_C'"
                >
                  <template slot-scope="scope">
                    <div v-if="citem.attrName.indexOf('DATE') > 0">
                      <span>{{ dateFormat(scope.row[citem.attrName]) }}</span>
                    </div>
                    <div v-else>
                      <span
                        :class="
                          scope.row['LIFECYCLE_DIR'] == 0 ? 'reject' : 'success'
                        "
                        >{{ scope.row[citem.attrName] }}</span
                      >
                    </div>
                  </template>
                </el-table-column>
              </template>
              <template v-else>
                <el-table-column
                  :label="citem.label"
                  :width="citem.width"
                  :prop="citem.attrName"
                  :sortable="sortBackData?'custom':citem.allowOrderby"
                  :key="idx + '_C'"
                >
                  <template slot-scope="scope">
                    <div v-if="citem.attrName.indexOf('DATE') > 0">
                      <span>{{ dateFormat(scope.row[citem.attrName]) }}</span>
                    </div>
                    <div v-else>
                      <span
                        :class="
                          scope.row['LIFECYCLE_DIR'] == 0 ? 'reject' : 'success'
                        "
                        >{{ scope.row[citem.attrName] }}</span
                      >
                    </div>
                  </template>
                </el-table-column>
              </template>
            </template>
          </template>
        </template>
        <el-table-column
          v-if="isshowOption"
          :label="$t('application.operation')"
          :width="optionWidth * 60"
        >
          <template slot="header">
            <el-button
              icon="el-icon-s-grid"
              size="small"
              @click="dialogFormShow"
              :title="$t('application.selectStr')"
            ></el-button>
            <template v-if="isShowChangeList">
              <!-- <el-dropdown trigger="click" style="overflow: visible">
                <el-button
                  type="primary"
                  plain
                  size="small"
                  :title="$t('application.selectList')"
                  icon="el-icon-more"
                ></el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    v-for="(item, idx) in customList"
                    :key="idx + '_Cz'"
                    @click.native="showConfigInfo(item)"
                    >{{ item.name }}</el-dropdown-item
                  >
                </el-dropdown-menu>
              </el-dropdown> -->
              <el-button
              icon="el-icon-more"
              size="small"
              @click="showCustomConfig"
              :title="$t('application.selectList')"
            ></el-button>
            </template>
            <el-button
              v-if="isshowCustom"
              icon="el-icon-setting"
              size="small"
              @click="showEditColumn"
              :title="$t('application.customColumn')"
            ></el-button>
          </template>
          <template slot-scope="scope">
            <slot name="optionButton" :data="scope">
              <slot name="customMoreOption" :data="scope">
                <template v-if="isShowMoreOption">
                  <el-dropdown trigger="click">
                    <el-button
                      type="primary"
                      plain
                      size="small"
                      :title="$t('application.more')"
                      icon="el-icon-more"
                    ></el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        v-if="showOptions.indexOf('查看内容') != -1"
                        icon="el-icon-reading"
                        @click.native="showItemContent(selectedRow)"
                        >{{ $t("application.viewContent") }}</el-dropdown-item
                      >
                      <el-dropdown-item
                        v-if="showOptions.indexOf('查看属性') != -1"
                        icon="el-icon-info"
                        @click.native="showItemProperty(scope.$index, selectedRow)"
                        >{{ $t("application.viewProperty") }}</el-dropdown-item
                      >
                      <el-dropdown-item
                        v-if="showOptions.indexOf('加入收藏夹') != -1"
                        icon="el-icon-circle-plus-outline"
                        @click.native="addToShoppingCar([selectedRow])"
                        >{{ $t("application.addCart") }}</el-dropdown-item
                      >
                      <el-dropdown-item
                        v-if="showOptions.indexOf('升版') != -1"
                        icon="el-icon-check"
                        @click.native="upgrade(selectedRow)"
                        >{{
                          $t("application.objectPermission6")
                        }}</el-dropdown-item
                      >
                      <slot name="dropdownItem" :data="scope"></slot>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </slot>
              <!-- showItemContent(scope.row) -->
              <el-button
                v-if="isShowPropertyButton"
                type="primary"
                plain
                size="small"
                :title="$t('application.property')"
                icon="el-icon-info"
                @click="showItemProperty(scope.$index,scope.row)"
              ></el-button>
            </slot>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="isshowPage"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100, 200, 500,1000]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="itemCount"
      ></el-pagination>
    </div>
  </div>
</template>
<script type="text/javascript">
import "@/utils/dialog";
import ShowProperty from "@/components/ShowProperty";
import ShowPropertyReadOnly from "@/components/ShowPropertyReadOnly"
import EcmCustomColumns from "@/components/ecm-custom-columns";
export default {
  name: "dataGrid",
  data() {
    return {
      rkey: 0,
      showConfigList: false,
      configName: "",
      configItem:{},
      refreshCustomView: true,
      inputColumn: false,
      customNames: [],
      customList: [],
      selectedName: "",
      leftData: [],
      selectedColumns: [],
      editColumn: false,
      columnsInfo: {
        checkAll: true,
        checkedCities: [],
        temCol: [],
        dialogFormVisible: false,
        isIndeterminate: false,
      },
      propertyVisible: false,
      currentPage: 1,
      
      showFields: [],
      selectedItemId: "",
      selectedRow: "",
      selectedRows:[],
      typeName: "",
      selectedKey: [],
      selectedIndex: "",
      // itemDataList:[],
      columnList: [],
      sysColumnInfo: [],
      itemCount: 0,
      formName: "",
      currentLanguage: "zh-cn",
      propertiesData: [],
      gridviewInfo: {
        gridviewName: "",
        isCustom: false,
        currentFolder: []
      },
      archiveMap:{},
      timer: null,
      currentDocIndex: 0,
      prevButtonDisabled:true,
      nextButtonDisabled:true,
      orderBy:"",
      cachePrefix:""
    };
  },
  props: {
    isInitData: { type: Boolean, default: true }, //是否初始化数据
    itemDataList: { type: Array, default: null },
    isEditProperty: { type: Boolean, default: true }, //属性页面中是否显示保存按钮
    // sysColumnInfo:{type: Array, default: null},
    // columnList: { type: Array, default: null },
    isshowicon: { type: Boolean, default: true }, //是否显示图标
    isshowOption: { type: Boolean, default: false },
    isshowSelection: { type: Boolean, default: true },
    tableHeight: { type: [String, Number], default: window.innerHeight - 408 },
    tableWidth: { type: [String, Number], default: "100%" },
    // itemCount: { type: [String, Number] },
    isshowPage: { type: Boolean, default: true },
    loading: { type: Boolean, default: false },
    gridViewName: { type: String, default: "" },
    isshowCustom: { type: Boolean, default: false },
    condition: { type: String, default: "" },
    dataUrl: { type: String, default: "" },
    parentId: { type: String, default: "" },
    isShowMoreOption: { type: Boolean, default: true }, //是否显示功能菜单
    isShowPropertyButton: { type: Boolean, default: true }, //是否显示属性按钮
    showOptions: { type: String, default: "查看内容,查看属性,加入收藏夹,升版" }, //功能菜单显示控制
    isShowChangeList: { type: Boolean, default: true }, //是否显示列表选择
    optionWidth: { type: Number, default: 3.5 }, //操作列宽度，放几个按钮
    pageSize: { type: Number, default: 20 },//每页显示数量
    folderId:{type:String,default:""},//目录ID
    isLoadGridInfo: { type: Boolean, default: true },
    showBatchCheck: { type: Boolean, default: false },
    sortBackData: { type: Boolean, default: false },//后台排序
    extparam:{type:Map,default:null}
  },
  watch: {
    showFields(val, oldVal) {
      //普通的watch监听
      //console.log("a: "+val, oldVal);
      let _self = this;
      _self.columnList.forEach((element) => {
        element.visibleType = 2;
      });
      val.forEach((element) => {
        let item = _self.getgriditem(element);
        if (item) {
          //console.log(element);
          item.visibleType = 1;
        }
      });
    },

    value(val) {
      this.selectedColumns = val;
    },
    selectedColumns(val) {
      this.$emit("input", val);
    },

    "$store.state.app.language": function (nv, ov) {
      this.currentLanguage = nv;
      this.loadGridInfo();
    },
  },
  components: {
    ShowProperty: ShowProperty,
    ShowPropertyReadOnly:ShowPropertyReadOnly,
    EcmCustomColumns: EcmCustomColumns,
  },
  mounted() {
    // this.ready();
    this.gridviewInfo.gridviewName = this.gridViewName;
    this.gridviewInfo.isCustom = false;
    this.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    //this.loadCustomName();
    if(this.isLoadGridInfo){
      this.loadGridInfo();
    }
    if (this.isInitData) {
      this.loadGridData();
    }
  },
  methods: {
    cleanData(){
      this.itemDataList = [];
    },
    //Matthew changes on 2021年1月26日15:48:46
    refreshMainConfigList(){
      this.loadCustomListConfig(this.gridviewInfo.gridviewName);
    },
    showCustomConfig(){
      this.showConfigList = true;
      this.$nextTick(function(){
        this.refreshMainConfigList();
      },100);
    },
    changeConfig(item){
      this.configItem = item
    },
    useConfig(){
      if(this.configItem.name=="默认"){
        localStorage.setItem(this.cachePrefix+this.gridViewName, this.configItem.id);
      }else{
        localStorage.setItem(this.cachePrefix+this.gridViewName, this.configItem.id+"_CUSTOM");
      }
      this.showConfigInfo(this.configItem);
      this.showConfigList= false;
    },
    // showCustomListConfig(val){
    //   let _self = this;
    //   _self.gridviewInfo.isCustom = true;
    //   _self.columnList = val;
    //   _self.loadGridData();
    // },
    loadCustomListConfig(val){
      let _self = this;
      let url = "/archive/getConfigList";
      let mp = new Map()
      mp.set("C_FROM",val)
      axios.post(url,JSON.stringify(mp)).then(function(response){
        _self.customList = [];
            if (response.data.code == 1) {
            _self.customList = response.data.data;
            _self.customList.push({"name":"默认","id":_self.gridViewName})
            //_self.gridviewInfo.gridviewName = _self.gridViewName;
            _self.gridviewInfo.isCustom = false;
          }
          })
    },

    loadCustomGridInfo(initGridName){
      let _self = this;
      _self.gridViewName = initGridName;
      var currentCustomConfig = localStorage.getItem(_self.cachePrefix + initGridName);
      if(currentCustomConfig==null || currentCustomConfig==undefined){
        _self.gridviewInfo.gridviewName = initGridName;
        _self.showConfigInfo({"id":initGridName,"name":"默认"})
      }else{
        if(currentCustomConfig.indexOf("_CUSTOM")>-1){
          _self.showConfigInfo({"id":currentCustomConfig.replace("_CUSTOM",""),"name":currentCustomConfig})
        }else{
          _self.showConfigInfo({"id":currentCustomConfig,"name":"默认"})
        }
      }
    },

    showConfigInfo(item){
      let id = item.id;
      let _self = this;
      var m = new Map();
      m.set("id", id);
      m.set("gridName", id);
      m.set("lang", _self.currentLanguage);
      let url="";
      if(item.name=="默认"){
        url = "/dc/getEcmCustomGridViewInfo";
      }else{
        url = "/archive/getConfigById";
      }
      axios.post(url,JSON.stringify(m)).then(function(response){
            if(response.data.code==1) {
              if(item.name=="默认"){
                _self.gridviewInfo.isCustom = false;
                _self.gridviewInfo.gridviewName = item.id;
                _self.columnList = response.data.customGridInfo;
              }else{
                _self.gridviewInfo.gridviewName = item.id+"_CUSTOM";
                _self.columnList = JSON.parse(response.data.data);
                _self.gridviewInfo.isCustom = true;
              }
              _self.columnList.forEach((element) => {
                if (element.visibleType == 1) {
                  _self.showFields.push(element.attrName);
                }
              });
              _self.$emit("changeGridName", _self.gridviewInfo.gridviewName);
              _self.loadGridData();
            }
          })
    },
    loadArchiveInfo(archiveType,gridView){
      let _self = this
      _self.configName=''
      _self.loadCustomListConfig(gridView)
      var archiveInfo = new Map();
      archiveInfo.set("C_FROM",gridView)
      archiveInfo.set("archiveType",archiveType)
      _self.archiveMap = archiveInfo;
      if(_self.$refs.ecmCustomColumns){
        _self.$nextTick(function(){
          _self.$refs.ecmCustomColumns.loadArchiveInfo();
        },100);
      }
    },
    //end
    //上一个文档
    onPrevDoc(){
      if(this.currentDocIndex>0){
        this.currentDocIndex --;
        this.showItemProperty(this.currentDocIndex, this.itemDataList[this.currentDocIndex]);
      }
    },
    //下一个文档
    onNextDoc(){
      if(this.currentDocIndex <  this.itemDataList.length-1){
        this.currentDocIndex ++;
        this.showItemProperty(this.currentDocIndex, this.itemDataList[this.currentDocIndex]);
      }
    },
    //刷新上一个、下一个按钮
    refreshBatchBotton(){
      let _self = this;
      if(_self.showBatchCheck && _self.selectedItemId && _self.selectedItemId != ""){
        if(_self.itemDataList && _self.itemDataList.length>0){
          for(var i=0;i< _self.itemDataList.length; i++){
            if(_self.selectedItemId == _self.itemDataList[i].ID){
              _self.prevButtonDisabled = false;
              _self.nextButtonDisabled = false;
              if(i==0){
                _self.prevButtonDisabled = true;
              }
              if(i == _self.itemDataList.length-1){
                _self.nextButtonDisabled = true;
              }
              return;
            }
          }
        }
      }
    },
    onPropertiesSaveSuccess(props) {
      this.$emit("onPropertiesSaveSuccess", props);
    },
    getPropertiesData() {
      let _self = this;
      clearInterval(_self.timer);
      _self.timer = setTimeout(() => {
        if (_self.$refs.ShowProperty) {
          _self.propertiesData = _self.$refs.ShowProperty.getFormData();
        }
      }, 100);
    },
    loadGridData() {
      this.loadGridData(null);
    },
    // 加载表格数据
    loadGridData(gvname) {
      if(this.dataUrl=="" || this.dataUrl  == null){
        return;
      }
      let _self = this;
      _self.selectedRows = [];
      // let tbHeight = _self.tableHeight;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", this.gridviewInfo.gridviewName);
      // m.set('folderId',indata.id);
      m.set('folderId',_self.folderId);
      m.set("condition", _self.condition);
      if (_self.parentId != "") {
        m.set("id", _self.parentId);
      }
      if(_self.extparam!=null){
        _self.extparam.forEach(function (value, key, map) {
           m.set(key,value);
        });
        
      }
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", _self.currentPage - 1);
      if(_self.sortBackData){
        m.set("orderBy", _self.orderBy);
      }else{
        m.set("orderBy", "");
      }
      
      axios
        .post(this.dataUrl, JSON.stringify(m))
        .then(function (response) {
          _self.itemDataList = response.data.data;
          _self.itemCount = response.data.pager ? response.data.pager.total : 0;
          _self.loading = false;
          setTimeout(() => {
            _self.tableHeight =  _self.tableHeight + (Math.floor(Math.random()*10)>4?1:-1);
          }, 100);
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },

    onCloseCustom() {
      let _self = this;
      _self.editColumn = false;
      _self.$nextTick(() => {
        _self.loadCustomName();
        _self.$forceUpdate();
        _self.rkey++;
        // location.reload();
      });
      _self.$refs.ecmCustomColumns.clearData();
    },
    deleteGridView() {
      let _self = this;
      var m = new Map();
      m.set("gridName", _self.gridViewName);
      m.set("NAME", _self.selectedName);
      m.set("lang", _self.currentLanguage);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/admin/deleteCustomGridView",
        })
        .then(function (response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.deleteSuccess"),
              duration: 2000,
              type: "Success",
            });
            _self.selectedName = "";
            _self.loadCustomName();
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    showCustomInfo(item) {
      let id = item.id;
      let _self = this;
      var m = new Map();
      m.set("gridId", id);
      m.set("lang", _self.currentLanguage);
      let url = "/dc/getOneEcmCustomGridViewInfo";
      axios
        .post(url, JSON.stringify(m))
        .then(function (response) {
          if (response.data.code == 1) {
            _self.gridviewInfo.gridviewName = item.name;
            _self.gridviewInfo.isCustom = true;
            _self.columnList = response.data.data;
            _self.loadGridData();
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    createCustomGrid() {
      let _self = this;
      if (_self.selectedName == "") {
        _self.$message({
          showClose: true,
          message: _self.$t("message.nameIsNull"),
          duration: 2000,
          type: "Error",
        });
        return;
      }
      var m = new Map();
      m.set("gridName", _self.gridViewName);
      m.set("NAME", _self.selectedName);

      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/admin/createOrUpdateGridView",
        })
        .then(function (response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "Success",
            });
          }
          _self.inputColumn = false;
          _self.loadCustomName();
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    loadCustomName() {
      let _self = this;
      let url = "/admin/getAllGridViewsOfCurrentUser";
      let params = {
        gridName: this.gridViewName,
      };
      axios
        .post(url, JSON.stringify(params))
        .then(function (response) {
          if (response.data.code == 1) {
            _self.customNames = response.data.data;
            _self.customList = response.data.data;

            _self.gridviewInfo.gridviewName = _self.gridViewName;
            _self.gridviewInfo.isCustom = false;
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    getGridViewInfo() {
      return this.gridviewInfo;
    },
    saveCustomColumn() {
      let _self = this;
      if (_self.selectedName == "") {
        _self.$message({
          showClose: true,
          message: _self.$t("message.nameIsNull"),
          duration: 2000,
          type: "Error",
        });
        return;
      }
      let mp = new Array();
      for (let i = 0; i < _self.selectedColumns.length; i++) {
        for (let j = 0; j < _self.sysColumnInfo.length; j++) {
          let obj = _self.sysColumnInfo[j];
          if (obj.attrName == _self.selectedColumns[i]) {
            mp.push(obj);
          }
        }
      }
      var m = new Map();
      m.set("gridName", _self.gridViewName);
      m.set("NAME", _self.selectedName);
      m.set("items", mp);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/admin/createOrUpdateGridView",
        })
        .then(function (response) {
          if (response.data.code == 1) {
            _self.$message({
              showClose: true,
              message: _self.$t("message.saveSuccess"),
              duration: 2000,
              type: "Success",
            });
          }
          _self.editColumn = false;
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    loadGridInfo() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", _self.gridViewName);
      m.set("lang", _self.currentLanguage);
      _self
        .axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8",
          },
          method: "post",
          data: JSON.stringify(m),
          url: "/dc/getEcmCustomGridViewInfo",
        })
        .then(function (response) {
          _self.columnList = response.data.customGridInfo;
          _self.sysColumnInfo = response.data.sysGridInfo;
          _self.columnList.forEach((element) => {
            if (element.visibleType == 1) {
              _self.showFields.push(element.attrName);
            }
          });
          // _self.tableHeight = "100%";
          _self.loading = false;
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    rightChange(value, direction, movedKeys) {
      if (direction == "left") {
        this.selectedKey = [];
      }
    },
    rightCheckChange(val) {
      this.selectedKey = val;
    },
    renderContent(h, option) {
      return h("span", { domProps: { title: option.label } }, option.label);
    },
    moveUp() {
      let _self = this;
      //选中值的下标
      if (_self.selectedKey.length == 1) {
        _self.selectedColumns.find((val, indexs, arr) => {
          if (val == _self.selectedKey[0]) {
            _self.selectedIndex = indexs;
          }
        });
        if (_self.selectedIndex == 0) {
          //当选择的项的下标为0，即第一个，则提醒没有上移的空间，选择其他项进行上移
          _self.$message({
            showClose: true,
            message: _self.$t("message.noUpper"),
            duration: 2000,
            type: "warning",
          });

          return;
        }
        // 上移-改变的数组（项和下标同时改变）

        let changeItem = JSON.parse(
          JSON.stringify(_self.selectedColumns[_self.selectedIndex - 1])
        );
        _self.selectedColumns.splice(_self.selectedIndex - 1, 1);
        _self.selectedColumns.splice(_self.selectedIndex, 0, changeItem);
      } else {
        _self.$message({
          showClose: true,
          message: _self.$t("message.oneDataOnly"),
          duration: 2000,
          type: "error",
        });
      }
    },
    moveDown() {
      let _self = this;
      //选中值的下标
      if (_self.selectedKey.length == 1) {
        _self.selectedColumns.find((val, indexs, arr) => {
          if (val == _self.selectedKey[0]) {
            _self.selectedIndex = indexs;
          }
        });
        if (_self.selectedIndex == _self.selectedColumns.length - 1) {
          //当选择的项的下标为0，即第一个，则提醒没有上移的空间，选择其他项进行上移
          _self.$message({
            showClose: true,
            message: _self.$t("message.noDowner"),
            duration: 2000,
            type: "warning",
          });

          return;
        }
        // 上移-改变的数组（项和下标同时改变）

        let changeItem = JSON.parse(
          JSON.stringify(_self.selectedColumns[_self.selectedIndex])
        );
        _self.selectedColumns.splice(_self.selectedIndex, 1);
        _self.selectedColumns.splice(_self.selectedIndex + 1, 0, changeItem);
      } else {
        _self.$message({
          showClose: true,
          message: _self.$t("message.oneDataOnly"),
          duration: 2000,
          type: "error",
        });
      }
    },
    showCreateName() {
      let _self = this;
      _self.selectedName = "";
      _self.inputColumn = true;
    },
    showEditColumn() {
      let _self = this;
      _self.editColumn = true;
      _self.$nextTick(() => {
        _self.$refs.ecmCustomColumns.loadArchiveInfo();
        _self.leftData = _self.generateData();
      },100);
    },
    generateData() {
      let _self = this;
      const data = [];
      for (let i = 0; i < _self.sysColumnInfo.length; i++) {
        data.push({
          key: _self.sysColumnInfo[i].attrName,
          label: _self.sysColumnInfo[i].label,
        });
      }
      return data;
    },
    leave() {
      var menu = document.querySelector("#menu");
      menu.style.display = "none";
    },
    //显示菜单
    showMenu(event) {
      // 鼠标右击触发事件
      var menu = document.querySelector("#menu");
      menu.style.display = "block";
      menu.style.left = event.clientX - 250 + "px";
      menu.style.top = event.clientY - 120 + "px";
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
    //升版
    upgrade(item) {
      let _self = this;

      // console.log('pagesize:', _self.pageSize);
      axios
        .post("/dc/upgradeDocument", item.ID)
        .then(function (response) {
          if (response.data.code == "1") {
            _self.$emit("upgradeFun", response.data.id);
          } else {
            _self.$message({
              showClose: true,
              message: response.data.message,
              duration: 2000,
              type: "warning",
            });
          }
        })
        .catch(function (error) {
          console.log(error);
          _self.loading = false;
        });
    },
    // 保存文档
    saveItem() {
      this.$refs.ShowProperty.saveItem();
    },
    //查看属性
    showItemProperty(i,indata) {
      let _self = this;
      
      _self.selectedItemId = indata.ID;
      _self.currentDocIndex = i;
      _self.propertyVisible = true;
      _self.refreshBatchBotton();
      _self.$nextTick(() => {
        if (_self.$refs.ShowProperty) {
          _self.$refs.ShowProperty.myItemId = indata.ID;
          _self.typeName = indata.TYPE_NAME;
          if (_self.typeName == "相关文件") {
            _self.$refs.ShowProperty.formName = _self.formName;
          } else {
            _self.$refs.ShowProperty.formName = "";
          }
          _self.$refs.ShowProperty.loadFormInfo();
          _self.getPropertiesData();
        }
      });
    },
    onSaved(indata) {
      if (indata == "update") {
        this.$message(this.$t("message.saveSuccess"));
        this.loadGridData();
        this.$emit("refreshdatagrid");
      } else {
        //this.$message("新建成功!");
      }
      if(!this.showBatchCheck){
        this.propertyVisible = false;
      }
    },
    showFieldOption() {
      let _self = this;
      _self.showFields = [];

      _self.columnList.forEach((element) => {
        if (element.visibleType == 1) {
          _self.showFields.push(element.attrName);
        }
      });
    },
    getgriditem(attrName) {
      let _self = this;
      let ret = null;
      _self.columnList.forEach((element) => {
        if (element.attrName == attrName) {
          ret = element;
          return;
        }
      });
      return ret;
    },
    //全选按钮
    handleCheckAllChange(val) {
      this.showFields = [];
      if (val) {
        this.columnList.forEach((element) => {
          this.showFields.push(element.attrName);
        });
      }
      this.columnsInfo.isIndeterminate = false;
    },
    //单个选中
    handleCheckedColsChange(value) {
      let checkedCount = value.length;
      this.columnsInfo.checkAll = checkedCount === this.columnList.length;
      this.columnsInfo.isIndeterminate =
        checkedCount > 0 && checkedCount < this.columnList.length;
    },
    //展示勾选弹框
    dialogFormShow() {
      this.showFieldOption();
      this.columnsInfo.dialogFormVisible = true;
    },
    confirmShow() {
      let _self = this;
      _self.columnList.forEach((element) => {
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
    handleCurrentChange(val) {
      this.currentPage = val;
      //console.log('handleCurrentChange', val);
      this.$emit("pagechange", this.currentPage);
      this.loadGridData();
    },
    // 分页 页数改变
    handleSizeChange(val) {
      this.currentPage = 1;
      this.pageSize = val;
      localStorage.setItem("docPageSize", val);
      //console.log('handleSizeChange', val);
      this.$emit("pagesizechange", this.pageSize);
      this.loadGridData();
    },
    sortchange(column) {
      //console.log(JSON.stringify(column));
      //console.log(column.column.property);
      //console.log(column.column.order); //ascending, descending
      if(column.column.order == null){
        this.orderBy = "";
      }else if(column.column.order == "ascending"){
        if(column.column.property == "C_STORE_CODING"){
          this.orderBy = "(" +column.column.property +'+0) ASC'
        }else{
          this.orderBy = column.column.property +' ASC'
        }
        
      }else{
        if(column.column.property == "C_STORE_CODING"){
          this.orderBy ="(" + column.column.property +'+0) DESC'
        }else{
           this.orderBy = column.column.property +' DESC'
        }
      }
      console.log("orderBy:"+this.orderBy);
      if(this.sortBackData){
        this.loadGridData();
      }
    },
    rowClick(row) {
      this.selectedRow = row;
      
      if(row&&this.isshowSelection && this.selectedRows && this.selectedRows.length<2){
        this.$refs.datatable.clearSelection();
        this.$refs.datatable.toggleRowSelection(row);
      }
      this.$emit("rowclick", row);
    },
    getCurrentIndex(row){
      var i=0;
      for(i=0;i<this.itemDataList.length;i++){
        if(this.itemDataList[i].ID==row.ID){
          return i;
        }
      }
      return -1;
    },
    dbclick(row, column, event) {
      this.showItemProperty(this.getCurrentIndex(row),row);
      this.$emit("dbclick", row);
    },

    selectChange(val) {
      this.selectedRows = val;
      this.$emit("selectchange", val);
    },
    //row 行对象，column 列对象,cell 单元格,event 事件对象
    cellMouseEnter(row, column, cell, event) {
      this.$emit("cellMouseEnter", row, column, cell, event);
    },
    //row 行对象，column 列对象,cell 单元格,event 事件对象
    cellMouseLeave(row, column, cell, event) {
      this.$emit("cellMouseLeave", row, column, cell, event);
    },
  },
};
</script>
<style>
.el-transfer-panel__item.el-checkbox {
  margin-left: 0px;
}
</style>
<style scoped>
/* :root {
   --scoll-height: (tableHeight)px;
}
.el-table__body-wrapper{
  height: var(--scoll-height);
} */
.success {
  color: "";
}
.reject {
  color: red;
}

/* #menu {
        width: 120px; 
        height: 100px;
        overflow: hidden; //隐藏溢出的元素 */
/* box-shadow: 0 1px 1px #888, 1px 0 1px #ccc;
        position: absolute; 
        display: none;
        background: #ffffff;
        z-index: 10;
    } */

/* .menu {
        width: 125px;
        height: 25px;
        line-height: 25px;
        text-indent: 10px;
        cursor: pointer;
    }
 
    .menu:hover {
        color: deeppink;
        text-decoration: underline;
    }
    .el-dropdown-link {
      cursor: pointer;
      color: #409EFF;
    }
    .hey-btn { display: inline-block; 
    background-color: #87CEEB; 
    color: white; 
    text-decoration: none; 
    font-family: 'Microsoft YaHei', sans-serif; 
    text-align: center; 
    border: 1px; 
    width: 2.5rem;
    height: 1.8rem;
    cursor: pointer; } */
.el-dropdown-link {
  height: 30px;
  border-radius: 4px;
  cursor: pointer;
  color: #409eff;
}

</style>