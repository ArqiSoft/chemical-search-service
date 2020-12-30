<template>
  <div class="container my-2">
    <h1>{{ header }}</h1>
    <div class="columns">
      <div class="col-6 col-mx-auto input-group">
        <input class="form-input" placeholder="Enter SMILES" autofocus v-model="filter" @keyup.enter="search"/>
        <button class="btn btn-primary input-group-btn" @click="search" :disabled="filter === null || filter === ''">
          Search
        </button>
      </div>
    </div>

    <div class="columns">
      <div class="col-6 col-mx-auto form-group">
        <label class="form-radio form-inline mx-2">
          <input type="radio" name="gender" v-bind:value="SearchType.Similarity" v-model="searchType"><i
            class="form-icon"></i> Similarity
        </label>
        <label class="form-radio form-inline mx-2">
          <input type="radio" name="gender" v-bind:value="SearchType.Exact" v-model="searchType"><i
            class="form-icon"></i> Exact
        </label>
        <label class="form-radio form-inline mx-2">
          <input type="radio" name="gender" v-bind:value="SearchType.Substructure" v-model="searchType"><i
            class="form-icon"></i> Substructure
        </label>
      </div>
    </div>

    <div class="columns" v-show="searchType === SearchType.Similarity">
      <div class="column col-6 col-mx-auto form-horizontal">
        <div class="form-group">
          <div class="col-auto my-auto px-1">
            <label class="form-label" for="threshold">Threshold:</label>
          </div>
          <div class="column col-10 pt-1">
            <input class="slider tooltip" id="threshold" type="range" step="0.1" min="0" max="1" value="0.9"
                   v-model="threshold" oninput="this.setAttribute('value', this.value);"/>
          </div>
        </div>
      </div>
    </div>

    <div class="columns">
      <div class="column col-10 col-mx-auto form-horizontal">
        <div class="form-group">
          <div class="col-auto px-1">
            <label class="form-label" for="rows">Show:</label>
          </div>
          <div class="column col-auto">
            <select id="rows" class="form-select" v-model="limit">
              <option>10</option>
              <option>25</option>
              <option>50</option>
            </select>
          </div>
          <div class="col-auto px-2 py-2" v-if="items.length"><span>Results: {{ items.length }}</span></div>
        </div>
      </div>
    </div>

    <div class="columns">
      <table class="column col-10 col-mx-auto table">
        <thead>
        <tr>
          <th class="molecule"></th>
          <th>ID</th>
          <th>Score</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in items" :key="item.Id">
          <td><img :src="item.ImageUrl" :alt="item.Id"></td>
          <td><a class="c-hand" @click="download(item.Id)">{{ item.Id }}</a></td>
          <td>{{ item.Score }}</td>
        </tr>
        </tbody>
      </table>
    </div>

<!--    <div class="columns" v-if="loading">-->
<!--      <progress class="column col-10 col-mx-auto progress" max="100"></progress>-->
<!--    </div>-->

    <div class="loading loading-lg mt-2" v-if="loading"></div>
    <div class="toast toast-error" v-if="error">
      <button class="btn btn-clear float-right" @click="error = null"></button>
      {{ error }}
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import axios from "axios";

enum SearchType {
  Similarity = 'SIMILARITYMATCH',
  Exact = 'EXACTMATCH',
  Substructure = 'SUBSTRUCTUREMATCH'
}

interface Result {
  Id: string;
  Score: number;
  ImageUrl?: string;
}

@Component
export default class Search extends Vue {
  @Prop() private header!: string;

  host = 'http://54.80.197.48:8080';
  filter = '';
  searchType: SearchType = SearchType.Similarity;
  SearchType = SearchType;
  threshold = 0.9;
  limit = 10;
  items: Result[] = [];
  loading = false;
  error: string | null = null;

  async search(): Promise<void> {
    const payload = {
      SmileFilter: this.filter,
      SearchType: this.searchType,
      Threshold: this.threshold,
      Limit: this.limit
    };
    try {
      this.items = [];
      this.error = null;
      this.loading = true;
      const response = await axios.post(`${this.host}/api/search`, payload);
      for (const item of response.data) {
        item.ImageUrl = await this.loadImage(item.Id);
      }
      this.items = response.data;
    } catch (e) {
      this.error = `Search: ${e}`;
    } finally {
      this.loading = false;
    }
  }

  async download(id: string): Promise<void> {
    const response = await axios.get(`${this.host}/api/structures/${id}/mol`, {responseType: 'blob'});
    
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${id}.txt`);
    document.body.appendChild(link);
    link.click();
  }
  
  private async loadImage(id: string): Promise<string> {
    const width = 200;
    const height = 200;
    try {
      const response = await axios.get(`${this.host}/api/structures/${id}/image?width=${width}&height=${height}`, {responseType: 'arraybuffer'});
      const binary = new Uint8Array(response.data).reduce((data, b) => data += String.fromCharCode(b), '');
      return `data:image/jpeg;base64,${btoa(binary)}`;
    } catch (e) {
      this.error = `Load image: ${e}`;
      return `https://dummyimage.com/${width}x${height}`; 
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.molecule {
  width: 50%;
}

table {
  border-collapse: separate;
  border-spacing: 0;
}

table th {
  border-bottom: 2px solid rgb(218, 222, 228);
}

table thead th {
  position: sticky;
  top: 0;
  background-color: white;
}

.toast {
  position: absolute;
  top: 20px;
  right: 20px;
  max-width: 400px;
  text-align: left;
}
</style>