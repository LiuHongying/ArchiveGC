<template>
<DataLayout>
    <template v-slot:header></template>
    <template v-slot:main="{ layout }">
  <div v-loading="loading">
    <el-dialog
      :title="$t('application.property')"
      :visible.sync="propertyVisible"
      @close="propertyVisible = false"
      width="90%"
    >
      <ShowPropertyReadOnly ref="ShowProperty" width="100%" v-bind:itemId="selectedItemId" :showTypeName="true"></ShowPropertyReadOnly>
      <div slot="footer" class="dialog-footer">
        <el-button @click="propertyVisible = false">{{$t('application.cancel')}}</el-button>
      </div>
    </el-dialog>
    <el-dialog 
      :title="$t('application.scendSearch')"
      :visible.sync="secondSearchVisible"
      @close="secondSearchVisible = false"
      width="60%"
      >
    <el-divider class="headerDivider"></el-divider>
    <div v-for="(term,idx) in terms" :key="idx+'_T'">
      <el-row style="margin-top:20px;margin-bottom:20px">
        <span class="ecmtitle" style="float:left;margin-right:30px;margin-left:20px">{{$t('application.'+term.fieldName)}}:</span>
          <el-checkbox-group  v-model="checkedTerms[term.index]">
            <template v-for="(gg,gidx) in term.groups" >
            <el-checkbox
              :label="gg.name"
              :key="term.fieldName+''+gg.name"
            >{{gg.name}}<el-badge :value="gg.value" class="item"></el-badge></el-checkbox>
            </template>
        </el-checkbox-group>
      </el-row>
      <el-divider class="contentDivider"></el-divider>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="secondSearchVisible = false" style="margin-right:20px">{{$t('application.cancel')}}</el-button>
      <el-button @click="handleScendSearch()" type="primary">{{$t('application.scendSearch')}}</el-button>
      </div>
    </el-dialog>
    <div v-if="searched==false" style="height:180px;padding-top:60px;text-align:center;padding-right:180px;">
      <img src="../../../static/img/search_bar.png" width="205px" height="120px" alt="logo" style="vertical-align: middle;" />
      <!-- <h1>{{$t("application.name")}}</h1>-->
      </div>
    <div class="searchInput" >
      <!-- <el-autocomplete
        class="inline-input"
        prefix-icon="el-icon-search"
        v-model="inputkey"
        :fetch-suggestions="querySearch"
        :placeholder="$t('application.placeholderSearch')"
        :trigger-on-focus="false"
        @select="handleSelect"
        @keyup.enter.native="enterDown"
        style="width:60%"
      ></el-autocomplete> -->
      <el-input prefix-icon="el-icon-search" v-model="inputkey" :placeholder="$t('application.placeholderSearch')" @keyup.enter.native="enterDown"
        style="width:35%;padding-top:10px;margin-left:20%"></el-input>
      <el-button style="margin-left:5px;width:60px;" type="primary" plain @click="enterDown">{{$t('application.search')}}</el-button>
        <el-radio style="margin-left:5px;margin-right:0px;" v-model="searchType" label="1" @change="enterDown">标题</el-radio>
        <el-radio style="margin-left:5px;margin-right:0px;" v-model="searchType" label="2" @change="enterDown">属性</el-radio>
        <el-radio style="margin-left:5px;margin-right:0px;" v-model="searchType" label="3" @change="enterDown">内容</el-radio>
      <el-button v-if="searched" style="margin-left:5px" type="primary" plain @click="beforeSecondSearch()">{{$t('application.scendSearch')}}</el-button>
    </div>
    <el-row>
      <el-col :span="13" style="text-align:left;margin-left:20%;margin-top:5px">
        <span>{{$t('application.docTypeName')}}:</span>
        <el-checkbox
          :indeterminate="isIndeterminate"
          v-model="checkAll"
          @change="handleCheckAllChange"
        >{{$t('application.selectAll')}}</el-checkbox>
  
        <el-checkbox-group v-model="checkedCards">
          <el-col :xs="8" :sm="6" :md="6" :lg="4" :xl="3" v-for="card in cards" :key="card.id">
          <el-checkbox
            :label="card.name"
            @change="handleCheckedTypeChange"
            style="padding-top:5px;"
          >{{card.name}}</el-checkbox>
          </el-col>
        </el-checkbox-group>
      </el-col>
    </el-row>
    <div v-if="searched">
      <div style="margin-top:5px;text-align:center">
        <span>{{$t('application.searchResult')}} {{itemCount}} &nbsp;&nbsp; {{$t('application.takeTime')}} {{searchTime}}{{$t('application.scend')}}</span>
      </div>
      <el-container>
        <!-- <el-col :span="3">
        <el-aside class="fixed" id="searchBar" width="160px">
          <el-row>
            <el-button
              size="small"
              type="primary"
              icon="el-icon-search"
              @click="handleScendSearch()"
              :disabled="searched==false"
            >{{$t('application.scendSearch')}}</el-button>
          </el-row>
          <div v-for="(term,idx) in terms" :key="idx+'_T'">
            <div v-if="term.fieldName != 'by_object_type'">
              <el-row>
                <el-row class="termHead">
                  <span>{{$t('application.'+term.fieldName)}}</span>
                </el-row>
                <el-row>
                  <div>
                    <el-checkbox-group v-model="checkedTerms[term.index]">
                      <div v-for="(gg,gidx) in term.groups" :key="gidx+'_G'">
                        <el-checkbox
                          :label="gg.name"
                          :key="term.fieldName+''+gg.name"
                        >{{gg.name}}({{gg.value}})</el-checkbox>
                      </div>
                    </el-checkbox-group>
                  </div>
                </el-row>
              </el-row>
            </div>
          </div>
        </el-aside>
        </el-col> -->
        <el-col :span="24" >
        <el-main>
          <!-- <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100, 200]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="itemCount"
          ></el-pagination>-->
          <el-table
            :data="dataList"
            style="width: 100%;margin-bottom: 20px;"
            :style="{minHeight: layout.height - 280 +'px'}"
            row-key="ID"
            border
            @header-dragend="onHeaderDragend"
            @row-dblclick="dbclick"
          >
            <el-table-column type="expand"  :key="'m_'+1">
              <template slot-scope="scope">
                <el-table
                  :data="scope.row.childrens"
                  border
                  :style="'width: '+(tableWidth+10)+'px'"
                  :show-header="false"
                >
                  <el-table-column :width="tableWidth+'px'">
                    <template slot-scope="scope">
                      <div style="color : #999999;" v-html="'摘要：'+scope.row.highlightText"></div>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </el-table-column>
            <el-table-column :label="$t('field.indexNumber')" width="60"  :key="'m_'+2">
              <template slot-scope="scope">
                <span>{{(currentPage-1) * pageSize + scope.$index+1}}</span>
              </template>
            </el-table-column>
            <el-table-column width="40"  :key="'m_'+3">
              <template slot-scope="scope">
                  <img v-if="scope.row.C_ITEM_TYPE=='案卷'"
                    :src="'./static/img/box.gif'"
                    :title="scope.row.TYPE_NAME"
                    border="0"
                  />
                  <img v-else :title="scope.row.FORMAT_NAME" :src="'./static/img/format/f_'+scope.row.FORMAT_NAME+'_16.gif'" border="0" />
              </template>
            </el-table-column>
            <template v-for="(citem,idx) in gridList">
              <template v-if="citem.visibleType==1">
                <template v-if="(citem.width+'').indexOf('%')>0">
                  <el-table-column
                    :label="citem.label"
                    :prop="citem.attrName"
                    :min-width="citem.width"
                    :sortable="citem.allowOrderby"
                    :key="idx+4"
                  >
                    <template slot-scope="scope">
                      <div v-if="citem.attrName.indexOf('DATE')>0">
                        <span>{{dateFormat(scope.row[citem.attrName])}}</span>
                      </div>
                      <div v-else>
                        <span>{{scope.row[citem.attrName]}}</span>
                      </div>
                    </template>
                  </el-table-column>
                </template>
                <template v-else>
                  <el-table-column
                    :label="citem.label"
                    :width="citem.width"
                    :prop="citem.attrName"
                    :sortable="citem.allowOrderby"
                    :key="idx+4"
                  >
                    <template slot-scope="scope">
                      <div v-if="citem.attrName.indexOf('_DATE')>0">
                        <span>{{dateFormat(scope.row[citem.attrName])}}</span>
                      </div>
                      <div v-else>
                        <span>{{scope.row[citem.attrName]}}</span>
                      </div>
                      <!--span>{{scope.row[citem.attrName]}}</span-->
                    </template>
                  </el-table-column>
                </template>
              </template>
            </template>
            <!--
          <el-table-column
            :label="$t('application.content')"
            minWidth="100%"
            >
              <template slot-scope="scope">
                <span>{{scope.row['filecontent']}}</span>
              </template>
          </el-table-column>
            -->
            <el-table-column :label="$t('application.operation')" width="140">
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
                  :title="$t('application.viewProperty')"
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
        </el-main>
        </el-col>
      </el-container>
    </div>
  </div>
  </template>
  </DataLayout>
</template>

<script type="text/javascript">
import DataLayout from "@/components/ecm-data-layout";
import ShowProperty from "@/components/ShowProperty";
import ShowPropertyReadOnly from "@/components/ShowPropertyReadOnly"
export default {
  name: "FullTextSearch",
  components: {
  	DataLayout: DataLayout,
    ShowProperty: ShowProperty,
    ShowPropertyReadOnly: ShowPropertyReadOnly
  },
  permit: 1,
  data() {
    return {
      imageFormat: "jpg,jpeg,bmp,gif,png",
      checkList: [],
      currentData: [],
      dataList: [],
      dataListFull: [],
      gridList: [],
      terms: [],
      inputkey: "",
      checkedCards: [],
      checkedFromRouter:[],
      cards: [],
      currentType: "",
      currentItem: "",
      cardsLabel: [],
      checkedTerms: [],
      checkAll: false,
      isScend: false,
      isSearched:false,
      searched: false,
      isIndeterminate: false,
      imageViewVisible: false,
      imageViewer: Object,
      imageArray: [],
      searchType: "1",
      searchTime: "",
      pageSize: 20,
      itemCount: 0,
      selectedItemId: "",
      propertyVisible: false,
      secondSearchVisible: false,
      selectedItems: [],
      currentPage: 1,
      loading: false,
      keywords: [],
      dialogVisible: false,
      tableHeight: window.innerHeight - 170,
      tableWidth: window.innerWidth - 270,
      formLabelWidth: "120px",
      loadData:false,
      searched:false,
    };
  },
  created() {
    let _self = this;
    var psize = localStorage.getItem("FTSearchPageSize");
    if (psize) {
      _self.pageSize = parseInt(psize);
    }
    try{
      var getMap = _self.$route.params.map;
      if (getMap != null) {
        _self.inputkey = getMap.get("inputkey");
        _self.checkedCards = getMap.get("checkedCards");
        _self.cardsLabel = getMap.get("checkedCards");
        _self.checkedFromRouter = getMap.get("checkedCards");
        _self.enterDown();
      }
    }catch(error){
      console.log(error)
    }
    _self.checkedCards = [];
    _self.loadCards();
    _self.loadGridInfo();
  },
  methods: {
    dbclick(row, column, event) {
      this.showItemProperty(row);
      this.$emit("dbclick", row);
    },
    enterDown() {
      this.currentPage = 1;
      this.search(false);
    },
    handleCheckAllChange(val) {
      this.checkedCards = val ? this.cardsLabel : [];
      this.isIndeterminate = false;
      this.enterDown();
    },
    handleCheckedTypeChange(value) {
      //console.log(JSON.stringify(value));
      let checkedCount = this.checkedCards.length;
      this.checkAll = checkedCount === this.cardsLabel.length;
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.cards.length;
      this.enterDown();
    },
    // 查看属性
    showItemProperty(indata) {
      let _self = this;
      _self.currentItem = indata;
      _self.selectedItemId = indata.ID;
      _self.propertyVisible = true;
      if (_self.$refs.ShowProperty) {
        _self.$refs.ShowProperty.myItemId = indata.ID;
        _self.$refs.ShowProperty.loadFormInfo();
      }
    },
    // 查看内容
    showItemContent(indata) {
      let condition = indata.ID;
      let href = this.$router.resolve({
        path: "/viewdoc",
        query: {
          id: condition,
          token: sessionStorage.getItem('access-token')
        }
      });
      //console.log(href);
      window.open(href.href, "_blank");
    },
    querySearch(queryString, cb) {
      let _self = this;
      
      axios
        .post("/search/getSuggestion", JSON.stringify(_self.inputkey))
        .then(function(response) {
          _self.keywords = response.data.data;
          if (_self.keywords) {
            var list = [{}];
            var i;
            for (i = 0; i < _self.keywords.length; i++) {
              var item = new Object();
              item.value = _self.keywords[i];
              list[i] = item;
            }
            _self.keywords = list;
          }
          cb(_self.keywords);
          //console.log(JSON.stringify(_self.taskList));
          _self.dialogVisible = true;
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    handleSelect(item) {
      //console.log(item);
      this.currentPage = 1;
      this.inputkey = item.value;
      this.search(false);
    },
    handleScendSearch() {
      //console.log(item);
      this.currentPage = 1;
      this.search(true);
      this.secondSearchVisible = false;
    },
    createFilter(queryString) {
      return keyword => {
        return (
          keyword.value.toLowerCase().indexOf(queryString.toLowerCase()) >= 0
        );
      };
    },
    // 分页 页数改变
    handleSizeChange(val) {
      this.pageSize = val;
      var maxPage = Math.floor(this.itemCount / val) + 1;
      this.currentPage =
        this.currentPage > maxPage ? maxPage : this.currentPage;
      localStorage.setItem("FTSearchPageSize", val);
      this.search(this.isScend);
      //console.log('handleSizeChange', val);
    },
    // 分页 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val;
      this.search(this.isScend);
    },
    loadCards(indata) {
      let _self = this;
      _self.loading = true;
      var m = new Map();
       axios
        .post("/admin/getArchivesFolder", 0)
        .then(function(response) {
          _self.cards = response.data.data;
          var i = 0;
          for (i = 0; i < _self.cards.length; i++) {
            _self.cardsLabel[i] = _self.cards[i].name;
          }
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
        // if(_self.checkedFromRouter.length>0){
        //   for(var i = 0; i <_self.checkedFromRouter.length;i++){
        //     var item = _self.checkedFromRouter[i]
        //     console.log(item)
        //       for(var j = 0;j<_self.cards.length;j++){
        //         if(item == _self.cards[j].label){
        //           _self.cards[j].description = true
        //           break;
        //         }
        //       }
        //   }
        //   console.log(_self.cards)
        // }
    },
    // 加载表格样式
    loadGridInfo() {
      let _self = this;
      _self.loading = true;
      var m = new Map();
      m.set("gridName", "FullTextGrid");
      m.set("lang", _self.getLang());
      axios
        .post("/dc/getGridViewInfo", JSON.stringify(m))
        .then(function(response) {
          _self.gridList = response.data.data;
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    saveItem() {},
    onSaved() {},
    search(isScend) {

      window.addEventListener("scroll", this.handleScroll);
      let _self = this;
      _self.searched = true;
      _self.loadData = true;
      _self.isScend = isScend;
      _self.loading = true;
      _self.isSearched = true;
      //console.log(JSON.stringify(_self.checkedCards));
      var m = new Map();
      m.set("keyword", _self.inputkey);
      m.set("pageSize", _self.pageSize);
      m.set("pageIndex", _self.currentPage - 1);
      m.set("typeNames", _self.checkedCards);
      m.set("searchType", _self.searchType);
      if (isScend) {
        var i;
        var terms = [];
        var len = 0;
        for (i = 0; i < _self.terms.length; i++) {
          let term = _self.terms[i];
          let cterm = _self.checkedTerms[i];
          if (cterm.length > 0) {
            var obj = new Object();
            obj.fieldName = term.fieldName;
            obj.values = cterm;
            terms[len] = obj;
            len++;
          }
        }
        if (len > 0) {
          m.set("terms", terms);
        }
      }
      axios
        .post("/search/searchByKeyword", JSON.stringify(m))
        .then(function(response) {
          _self.dataList = response.data.data.list;
          _self.searchTime = response.data.data.searchTime;
          _self.itemCount = response.data.total;
          let myterms = response.data.data.aggregations;
          //console.log(JSON.stringify(_self.dataList));
          _self.dataList.map(x => {
            var child = new Object();
            child.highlightText = x.highlightText;
            child.parentId = x.ID;
            x.childrens = new Array(child);
          });
          var i;
          _self.checkedTerms = [];
          // for(term in _self.terms){
          for (i = 0; i < myterms.length; i++) {
            let term = myterms[i];
            term.index = i;
            _self.checkedTerms[i] = [];
            var gg = JSON.stringify(term.groups);
            //console.log(gg);
            gg = gg.replace("{", "");
            gg = gg.replace("}", "");
            //console.log(gg);
            var items = gg.split(",");
            var gitem;
            var newGroup = [];
            var j;
            for (j = 0; j < items.length; j++) {
              var val = items[j].split(":");
              gitem = new Object();
              gitem.name = val[0].replace('"', "").replace('"', "");
              gitem.value = val[1];
              newGroup[j] = gitem;
            }
            term.groups = newGroup;
          }
          _self.terms = myterms;
          _self.searched = true;
          // console.log(JSON.stringify(_self.checkedTerms));
          _self.loading = false;
          _self.loadData = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    handleScroll() {
      var scrollTop =
        window.pageYOffset ||
        document.documentElement.scrollTop ||
        document.body.scrollTop;
      var offsetTop = document.querySelector('#searchBar');
      if(scrollTop<=200){
      offsetTop = 300 - Number(scrollTop);
      document.querySelector('#searchBar').style.top = offsetTop+'px';
      }else{
        document.querySelector('#searchBar').style.top = '100px';
      }
    },
    beforeSecondSearch(){
      let _self = this;
      if(_self.isSearched){
        _self.secondSearchVisible = true;
      }else{
        _self.$message("还未搜索，不能进行二次搜索!");
      }
      
    }
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}

.searchInput {
  display: inline;
  padding-top:10px;
  text-align: center;
}
.docType {
  display: inline;
  font-size: 14px;
}
.termHead {
  background-color: rgb(196, 225, 255);
}
.el-aside {
  background-color: rgb(231, 236, 241);
  position:fixed;
}
</style>
<style>
.el-button+.el-button {
    margin-left: 1px;
}
.el-divider.headerDivider{
     margin: 8px 0;
     background: 0 0;
     border-top: 2px solid #9fa0a1;
 } 
 .el-divider.contentDivider{
     margin: 8px 0;
     background: 0 0;
     border-top: 1px dashed #9fa0a1;
 } 
 .searchhighlight{
   background-color: #fceb01;
 }
 .ecmtitle{
  font-size: 16px;
  font-weight: bold;
}
</style>
